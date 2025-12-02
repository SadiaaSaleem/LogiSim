package edu.uni.logisim.service;

import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.component.io.InputSwitch;
import edu.uni.logisim.domain.component.io.LedOutput;
import edu.uni.logisim.domain.simulation.SimulationContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides functionality to generate truth tables and boolean expressions for circuits.
 * 
 * <p>This service analyzes circuits by:
 * <ul>
 *   <li>Finding all input switches and output LEDs</li>
 *   <li>Generating all possible input combinations</li>
 *   <li>Simulating the circuit for each combination</li>
 *   <li>Recording input/output values in a truth table</li>
 *   <li>Optionally deriving boolean expressions from the truth table</li>
 * </ul>
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class TruthTableService {
    
    /**
     * Generates a complete truth table for the given circuit.
     * 
     * <p>The method:
     * <ol>
     *   <li>Identifies all InputSwitch components as inputs</li>
     *   <li>Identifies all LedOutput components as outputs</li>
     *   <li>Generates all 2^n input combinations (where n is the number of inputs)</li>
     *   <li>Simulates the circuit for each combination</li>
     *   <li>Records the results in a truth table</li>
     * </ol>
     * 
     * @param circuit the circuit to analyze
     * @return a TruthTable containing all input/output combinations
     */
    public TruthTable generateTruthTable(Circuit circuit) {
        // Find all input switches and output LEDs
        List<InputSwitch> inputs = new ArrayList<>();
        List<LedOutput> outputs = new ArrayList<>();
        
        for (Component component : circuit.getComponents()) {
            if (component instanceof InputSwitch) {
                inputs.add((InputSwitch) component);
            } else if (component instanceof LedOutput) {
                outputs.add((LedOutput) component);
            }
        }
        
        if (inputs.isEmpty() || outputs.isEmpty()) {
            return new TruthTable();
        }
        
        TruthTable table = new TruthTable();
        
        // Set input names
        for (InputSwitch input : inputs) {
            table.addInputColumn(input.getName());
        }
        
        // Set output names
        for (LedOutput output : outputs) {
            table.addOutputColumn(output.getName());
        }
        
        // Generate all input combinations
        int numInputs = inputs.size();
        int numCombinations = 1 << numInputs; // 2^numInputs
        
        SimulationContext context = new SimulationContext(circuit);
        
        for (int i = 0; i < numCombinations; i++) {
            // Reset all component states and port values first (important for accurate results)
            for (Component component : circuit.getComponents()) {
                if (component instanceof InputSwitch) {
                    ((InputSwitch) component).setState(false);
                }
                // Reset all port values to ensure clean state
                if (component.getInputPorts() != null) {
                    for (edu.uni.logisim.domain.connector.Port port : component.getInputPorts()) {
                        port.setValue(false);
                    }
                }
                if (component.getOutputPorts() != null) {
                    for (edu.uni.logisim.domain.connector.Port port : component.getOutputPorts()) {
                        port.setValue(false);
                    }
                }
            }
            
            // Reset all connector values
            for (edu.uni.logisim.domain.connector.Connector connector : circuit.getConnectors()) {
                connector.setValue(false);
            }
            
            // Set input values based on binary representation
            for (int j = 0; j < numInputs; j++) {
                boolean value = (i & (1 << (numInputs - 1 - j))) != 0;
                inputs.get(j).setState(value);
                // CRITICAL: Execute input switch immediately to propagate its output
                inputs.get(j).execute();
            }
            
            // Run multiple simulation steps to ensure signals fully propagate
            // For simple circuits, 2-3 steps should be enough
            // This ensures signals propagate through all levels of gates
            for (int step = 0; step < 5; step++) {
                context.step();
            }
            
            // Read output values
            List<Boolean> outputValues = new ArrayList<>();
            for (LedOutput output : outputs) {
                // Ensure LED executes to read the latest input value
                output.execute();
                outputValues.add(output.isLit());
            }
            
            // Add row to truth table
            List<Boolean> inputValues = new ArrayList<>();
            for (InputSwitch input : inputs) {
                inputValues.add(input.getState());
            }
            
            table.addRow(inputValues, outputValues);
        }
        
        return table;
    }
    
    /**
     * Derives a boolean expression from a truth table using Sum of Products (SOP) method.
     * 
     * <p>This method generates a boolean expression in Sum of Products form:
     * - Each row where output is true becomes a product term (minterm)
     * - All product terms are combined with OR operations
     * 
     * @param table the truth table to derive expression from
     * @param outputIndex the index of the output column (0 for first output)
     * @return the boolean expression as a string (e.g., "A·B + A·B'")
     */
    public String deriveBooleanExpression(TruthTable table, int outputIndex) {
        if (table == null || table.getRows().isEmpty()) {
            return "No expression (empty truth table)";
        }
        
        if (outputIndex < 0 || outputIndex >= table.getOutputColumns().size()) {
            return "Invalid output index";
        }
        
        List<String> inputNames = table.getInputColumns();
        List<TruthTable.Row> rows = table.getRows();
        List<String> productTerms = new ArrayList<>();
        
        // Find all rows where the output is true (logic 1)
        for (TruthTable.Row row : rows) {
            List<Boolean> outputs = row.getOutputs();
            if (outputIndex < outputs.size() && outputs.get(outputIndex)) {
                // This row has output = true, create a product term
                List<String> terms = new ArrayList<>();
                List<Boolean> inputs = row.getInputs();
                
                for (int i = 0; i < inputs.size() && i < inputNames.size(); i++) {
                    String varName = inputNames.get(i);
                    boolean inputValue = inputs.get(i);
                    
                    if (inputValue) {
                        terms.add(varName); // Variable is true
                    } else {
                        terms.add(varName + "'"); // Variable is false (negated)
                    }
                }
                
                // Combine terms with AND (·)
                if (!terms.isEmpty()) {
                    productTerms.add(String.join("·", terms));
                }
            }
        }
        
        // If no true outputs, expression is always false
        if (productTerms.isEmpty()) {
            return "0"; // Always false
        }
        
        // Check if all outputs are true (always true)
        boolean allTrue = true;
        for (TruthTable.Row row : rows) {
            List<Boolean> outputs = row.getOutputs();
            if (outputIndex < outputs.size() && !outputs.get(outputIndex)) {
                allTrue = false;
                break;
            }
        }
        if (allTrue) {
            return "1"; // Always true
        }
        
        // Combine all product terms with OR (+)
        return String.join(" + ", productTerms);
    }
    
    /**
     * Represents a truth table with input and output columns.
     * 
     * <p>A truth table contains:
     * <ul>
     *   <li>Input column names (from InputSwitch components)</li>
     *   <li>Output column names (from LedOutput components)</li>
     *   <li>Rows representing all input/output combinations</li>
     * </ul>
     * 
     * @author LogiSim Development Team
     * @version 1.0
     */
    public static class TruthTable {
        private List<String> inputColumns;
        private List<String> outputColumns;
        private List<Row> rows;
        
        public TruthTable() {
            this.inputColumns = new ArrayList<>();
            this.outputColumns = new ArrayList<>();
            this.rows = new ArrayList<>();
        }
        
        /**
         * Adds an input column to the truth table.
         * 
         * @param name the name of the input column
         */
        public void addInputColumn(String name) {
            inputColumns.add(name);
        }
        
        /**
         * Adds an output column to the truth table.
         * 
         * @param name the name of the output column
         */
        public void addOutputColumn(String name) {
            outputColumns.add(name);
        }
        
        /**
         * Adds a row to the truth table.
         * 
         * @param inputs the input values for this row
         * @param outputs the output values for this row
         */
        public void addRow(List<Boolean> inputs, List<Boolean> outputs) {
            rows.add(new Row(inputs, outputs));
        }
        
        /**
         * Gets the list of input column names.
         * 
         * @return the input column names
         */
        public List<String> getInputColumns() {
            return inputColumns;
        }
        
        /**
         * Gets the list of output column names.
         * 
         * @return the output column names
         */
        public List<String> getOutputColumns() {
            return outputColumns;
        }
        
        /**
         * Gets all rows in the truth table.
         * 
         * @return the list of rows
         */
        public List<Row> getRows() {
            return rows;
        }
        
        /**
         * Represents a single row in the truth table.
         * Each row contains input values and corresponding output values.
         */
        public static class Row {
            private List<Boolean> inputs;
            private List<Boolean> outputs;
            
            /**
             * Creates a new truth table row.
             * 
             * @param inputs the input values for this row
             * @param outputs the output values for this row
             */
            public Row(List<Boolean> inputs, List<Boolean> outputs) {
                this.inputs = inputs;
                this.outputs = outputs;
            }
            
            /**
             * Gets the input values for this row.
             * 
             * @return the input values
             */
            public List<Boolean> getInputs() {
                return inputs;
            }
            
            /**
             * Gets the output values for this row.
             * 
             * @return the output values
             */
            public List<Boolean> getOutputs() {
                return outputs;
            }
        }
    }
}

