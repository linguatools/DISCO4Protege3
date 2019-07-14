/*******************************************************************************
 *   Copyright (C) 2010 Peter Kolb
 *   peter.kolb@linguatools.org
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *   use this file except in compliance with the License. You may obtain a copy
 *   of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *   License for the specific language governing permissions and limitations
 *   under the License.
 *
 ******************************************************************************/
package de.linguatools.disco.protege;

import de.linguatools.disco.DISCO;
import de.linguatools.disco.ReturnDataBN;
import de.linguatools.disco.ReturnDataCol;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.widget.*;
import java.awt.datatransfer.Clipboard;
import java.io.File;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * the DISCO Word Spaces Tab
 * @author peter.kolb@linguatools.org
 */
public class DISCOWordSpaces extends AbstractTabWidget {

    private JTextField searchTF;
    private JLabel displayWordL;
    private JTextField frequencyTF;
    private JTextField indexTF;
    private JTextField numberOfResultsTF;
    private JTable resultTable;
    private MyTableModel tableModel;
    private JFileChooser dirFileChooser = new JFileChooser();
    private JTextArea infoBox;
    private JButton searchB;
    private JButton simB;
    private JTextField simWord1TF;
    private JTextField simWord2TF;
    private JTextField simScore1TF;
    private JTextField simScore2TF;
    private DISCO disco;

    /***************************************************************************
     * Initialize the Tab Widget
     **************************************************************************/
    public void initialize() {

        final JPanel main = this;

        // Disco
        disco = new DISCO();

        // initialize the tab label
        setLabel("DISCOWordSpaces");
        ImageIcon icon = new ImageIcon(this.getClass().
                getResource("/images/discoIcon.gif"));
        setIcon(icon);

        // choose a word space directory
        dirFileChooser.setDialogTitle("Choose a word space directory");
        dirFileChooser.setName("dirFileChooser");
        JPanel panelR1 = new JPanel();
        panelR1.setLayout(new BoxLayout(panelR1, BoxLayout.PAGE_AXIS));

        JLabel directoryL = new JLabel("word space directory:");
        directoryL.setAlignmentX(LEFT_ALIGNMENT);
        JPanel panelR1_1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panelR1_1.setAlignmentX(LEFT_ALIGNMENT);
        panelR1_1.add(directoryL);

        JPanel panelR1_2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panelR1_2.setAlignmentX(LEFT_ALIGNMENT);

        indexTF = new JTextField(40);
        indexTF.setAlignmentX(LEFT_ALIGNMENT);
        final JButton chooseB = new JButton("Browse");
        chooseB.setAlignmentX(LEFT_ALIGNMENT);
        chooseB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == chooseB) {
                    dirFileChooser.setFileSelectionMode(
                            dirFileChooser.DIRECTORIES_ONLY);
                    int returnVal = dirFileChooser.showOpenDialog(main);
                    if (returnVal == dirFileChooser.APPROVE_OPTION) {
                        File file = dirFileChooser.getSelectedFile();
                        indexTF.setText(file.getPath());
                    }
                }
            }
        });
        panelR1.add(panelR1_1);
        panelR1_2.add(indexTF);
        panelR1_2.add(chooseB);
        panelR1.add(panelR1_2);

        // number of results
        JPanel panelR2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panelR2.setAlignmentX(LEFT_ALIGNMENT);
        JLabel numberOfResultsL = new JLabel("maximum number of results: ");
        numberOfResultsTF = new JTextField(4);
        numberOfResultsTF.setText("50");
        panelR2.add(numberOfResultsL);
        panelR2.add(numberOfResultsTF);

        // word space directory and number of results go together in one panel
        JPanel panelR12 = new JPanel();
        panelR12.setLayout(new BoxLayout(panelR12, BoxLayout.PAGE_AXIS));
        panelR12.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        panelR12.setAlignmentX(LEFT_ALIGNMENT);
        panelR12.setBorder(new EtchedBorder());
        panelR12.add(panelR1);
        panelR12.add(panelR2);

        // display word and frequency
        JPanel panelL2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panelL2.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        JLabel frequencyL = new JLabel("corpus frequency = ");
        frequencyTF = new JTextField(6);
        frequencyTF.setEditable(false);
        frequencyTF.setEnabled(true);
        JLabel displayWordInfoL = new JLabel("  The search word was: ");
        displayWordL = new JLabel();
        panelL2.add(frequencyL);
        panelL2.add(frequencyTF);
        panelL2.add(displayWordInfoL);
        panelL2.add(displayWordL);

        // table with results
        tableModel = new MyTableModel();
        resultTable = new JTable(tableModel);
        JScrollPane resultTablePane = new JScrollPane(resultTable);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.setCellSelectionEnabled(true);
        TableColumn column = null;
        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            column = resultTable.getColumnModel().getColumn(i);
            if (i == 1 || i == 4) {
                column.setPreferredWidth(100);
            } else {
                column.setPreferredWidth(10);
            }
        }
        TransferHandler th = resultTable.getTransferHandler();
        if (th != null) {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            th.exportToClipboard(resultTable, cb, TransferHandler.COPY);
        }

        // search for word in Disco
        JPanel panelL1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panelL1.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        JLabel searchL = new JLabel("enter word (case sensitive!): ");
        searchTF = new JTextField(20);
        searchTF.addActionListener(new SearchActionListener());
        // create the button
        searchB = new JButton("Search");
        searchB.addActionListener(new SearchActionListener());
        panelL1.add(searchL);
        panelL1.add(searchTF);
        panelL1.add(searchB);

        // compute similarity between two input words
        JLabel simLabel1 = new JLabel("compute similarity between");
        JLabel simLabel2 = new JLabel(" and ");
        JLabel simLabel3 = new JLabel("DISCO1: ");
        JLabel simLabel4 = new JLabel("DISCO2: ");
        simLabel1.setAlignmentX(LEFT_ALIGNMENT);
        simLabel2.setAlignmentX(LEFT_ALIGNMENT);
        simLabel3.setAlignmentX(LEFT_ALIGNMENT);
        simLabel4.setAlignmentX(LEFT_ALIGNMENT);
        simWord1TF = new JTextField(20);
        simWord1TF.setEditable(true);
        simWord1TF.setEnabled(true);
        simWord1TF.setAlignmentX(LEFT_ALIGNMENT);
        simWord2TF = new JTextField(20);
        simWord2TF.setEditable(true);
        simWord2TF.setEnabled(true);
        simWord2TF.setAlignmentX(LEFT_ALIGNMENT);
        simScore1TF = new JTextField(10);
        simScore1TF.setEditable(false);
        simScore1TF.setEnabled(true);
        simScore1TF.setAlignmentX(LEFT_ALIGNMENT);
        simScore2TF = new JTextField(10);
        simScore2TF.setEditable(false);
        simScore2TF.setEnabled(true);
        simScore2TF.setAlignmentX(LEFT_ALIGNMENT);
        simB = new JButton("Compute");
        simB.setAlignmentX(LEFT_ALIGNMENT);
        simB.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == simB) {
                    String w1 = simWord1TF.getText();
                    String w2 = simWord2TF.getText();
                    if (w1.equals("") || w2.equals("")) {
                        infoBox.setText("Please enter two words to compare!\n");
                        return;
                    }
                    String index = indexTF.getText();
                    if (index.equals("")) {
                        infoBox.setText("Please select a word space directory!\n");
                        return;
                    }
                    try {
                        String s1 = String.valueOf(
                                disco.firstOrderSimilarity(index, w1, w2));
                        if (s1.equals("-1.0")) {
                            simScore1TF.setText("");
                            infoBox.setText("One of the words \"" + w1 +
                                    "\" and \"" + w2 + "\" was not found.");
                        }
                        String s2 = String.valueOf(
                                disco.secondOrderSimilarity(index, w1, w2));
                        if (s2.equals("-1.0")) {
                            simScore2TF.setText("");
                            infoBox.setText("One of the words \"" + w1 +
                                    "\" and \"" + w2 + "\" was not found.");
                            return;
                        }
                        simScore1TF.setText(s1);
                        simScore2TF.setText(s2);
                        infoBox.setText("");
                    } catch (IOException ex) {
                        Logger.getLogger(DISCOWordSpaces.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        // put together all the components
        JPanel panelR3 = new JPanel();
        panelR3.setLayout(new BoxLayout(panelR3, BoxLayout.PAGE_AXIS));
        panelR3.setMaximumSize(new Dimension(Short.MAX_VALUE, 80));
        panelR3.setBorder(new EtchedBorder());
        panelR3.setAlignmentX(LEFT_ALIGNMENT);

        JPanel panelR3_0 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panelR3_0.setAlignmentX(LEFT_ALIGNMENT);
        panelR3_0.add(simLabel1);
        panelR3.add(panelR3_0);

        JPanel panelR3_1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panelR3_1.setAlignmentX(LEFT_ALIGNMENT);
        panelR3_1.add(simWord1TF);
        panelR3_1.add(simLabel2);

        JPanel panelR3_2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panelR3_2.setAlignmentX(LEFT_ALIGNMENT);
        panelR3_2.add(simWord2TF);

        panelR3.add(panelR3_1);
        panelR3.add(panelR3_2);
        JPanel panelR3_3 = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panelR3_3.setAlignmentX(LEFT_ALIGNMENT);
        panelR3_3.add(simLabel3);
        panelR3_3.add(simScore1TF);
        panelR3_3.add(simLabel4);
        panelR3_3.add(simScore2TF);
        panelR3_3.add(simB);
        panelR3.add(panelR3_3);

        // Info box
        infoBox = new JTextArea();
        infoBox.setAlignmentX(LEFT_ALIGNMENT);
        infoBox.setEditable(false);
        infoBox.setEnabled(true);
        infoBox.setForeground(Color.RED);
        infoBox.setBorder(new EtchedBorder());

        // About box
        JTextArea aboutBox = new JTextArea();
        aboutBox.setAlignmentX(LEFT_ALIGNMENT);
        aboutBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        aboutBox.setEditable(false);
        aboutBox.setEnabled(true);
        aboutBox.setBorder(new EtchedBorder());
        aboutBox.setText("DISCO version 1.1\nWord spaces available at\n"
                + "www.linguatools.de/disco/disco-download_en.html");

        // add the components to the tab widget
        JPanel panelLeft = new JPanel();
        panelLeft.setLayout(new BoxLayout(panelLeft, BoxLayout.PAGE_AXIS));
        panelLeft.setPreferredSize(new Dimension(600, 500));
        panelLeft.add(panelL1);
        panelLeft.add(panelL2);
        panelLeft.add(resultTablePane);
        JPanel panelRight = new JPanel();
        panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.PAGE_AXIS));
        panelRight.setMaximumSize(new Dimension(300, Short.MAX_VALUE));
        panelRight.add(panelR12);
        panelRight.add(panelR3);
        panelRight.add(infoBox);
        panelRight.add(aboutBox);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(panelLeft);
        add(panelRight);

    }

    /***************************************************************************
     * Dummy
     * @param project
     * @param errors
     * @return
     **************************************************************************/
    public static boolean isSuitable(Project project, Collection errors) {
        boolean isSuitable = true;
        return isSuitable;
    }

    /***************************************************************************
     * Main
     * @param args
     **************************************************************************/
    public static void main(String[] args) {
        edu.stanford.smi.protege.Application.main(args);
    }

    /***************************************************************************
     * inner class: search DISCO action
     **************************************************************************/
    class SearchActionListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == searchB || event.getSource() == searchTF) {
                try {
                    String word = searchTF.getText();
                    String index = indexTF.getText();
                    int max = 50;
                    boolean resetInfoBox = true;
                    try {
                        max = Integer.parseInt(numberOfResultsTF.getText());
                    } catch (NumberFormatException ex) {
                        infoBox.setText("Please enter an integer number in the" +
                                " \"number of results\" field!\n"
                                + "Number of results set to default value = 50\n"
                                + "Input \"" + numberOfResultsTF.getText() +
                                "\" was ignored.\n\n");
                        numberOfResultsTF.setText("50");
                        resetInfoBox = false;
                        max = 50;
                    }
                    if (max < 1) {
                        infoBox.setText("Please enter a number > 0 in the field" +
                                " \"number of results\"!\n");
                        return;
                    }
                    if (word.equals("")) {
                        infoBox.setText("Please enter a word in the search " +
                                "field!\n");
                        return;
                    }
                    if (index.equals("")) {
                        infoBox.setText("Please select a word space directory!\n");
                        return;
                    }
                    if (numberOfResultsTF.getText().equals("")) {
                        infoBox.setText("Please enter the desired number of " +
                                "results!\n");
                        return;
                    }
                    // lookup its frequency in Disco
                    int f = disco.frequency(index, word);
                    frequencyTF.setText(String.valueOf(f));
                    if (f == 0) {
                        infoBox.setText("The word \"" + word + "\" was not found.\n");
                        return;
                    }
                    displayWordL.setText(word);
                    // lookup the similar words in Disco
                    ReturnDataBN resDsb = disco.similarWords(index, word);
                    // lookup the collocations in Disco
                    ReturnDataCol[] resCol = disco.collocations(index, word);
                    tableModel.newTableContent(resDsb, resCol, max);
                    if (resetInfoBox == true) {
                        infoBox.setText("");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(DISCOWordSpaces.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /***************************************************************************
     * inner class: Table Data Model
     **************************************************************************/
    class MyTableModel extends AbstractTableModel {

        // Data model
        private String[] columnNames = {"rank", "collocation", "significance",
            "rank", "similar word", "score"};
        // Vector containing row vectors of fixed length 6
        private Vector data = new Vector();

        // Interfaces
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.size();
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return ((Vector) data.get(row)).get(col);
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            Vector buffer = (Vector) data.get(row);
            buffer.set(col, value);
            data.set(row, buffer);
            fireTableCellUpdated(row, col);
        }

        public void updateRow(Vector rowVector, int row) {
            data.set(row, rowVector);
            fireTableRowsUpdated(row, row);
        }

        public void appendRow(Vector rowVector) {
            data.add(rowVector);
            int r = getRowCount();
            fireTableRowsInserted(r, r);
        }

        public void deleteRow(int row) {
            data.remove(row);
            fireTableRowsDeleted(row, row);
        }

        public void newTableContent(ReturnDataBN resDsb, ReturnDataCol[] resCol,
                int max) {
            // delete old table contents
            data.clear();
            // copy new content from DISCO results
            for (int k = 0; k < max; k++) {
                Vector buffer = new Vector();
                if (k < resCol.length) {
                    buffer.add(k + 1);
                    buffer.add(resCol[k].word);
                    String v = String.valueOf(resCol[k].value);
                    if (!v.matches(".*?\\..*?")) {
                        v = v + ".0";
                    } else {
                        // keep only one decimal place
                        v = v.substring(0, v.indexOf('.') + 2);
                    }
                    buffer.add(Float.parseFloat(v));
                } else {
                    buffer.add(null);
                    buffer.add(null);
                    buffer.add(null);
                }
                if (k + 1 < resDsb.words.length) {
                    buffer.add(k + 1);
                    buffer.add(resDsb.words[k + 1]);
                    String v = resDsb.values[k + 1];
                    buffer.add(Float.parseFloat("0." + v));
                } else {
                    buffer.add(null);
                    buffer.add(null);
                    buffer.add(null);
                }
                data.add(buffer);
            }
            // update table
            fireTableDataChanged();
        }
    }
}
