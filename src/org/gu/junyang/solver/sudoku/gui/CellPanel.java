/**
 * Copyright (C) 2011 Junyang Gu <mikejyg@gmail.com>
 * 
 * This file is part of iSudokuSolver.
 *
 * iSudokuSolver is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * iSudokuSolver is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iSudokuSolver.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gu.junyang.solver.sudoku.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.gu.junyang.solver.sudoku.Cell;

/**
 *
 */
public class CellPanel extends javax.swing.JPanel {

	Logger logger=Logger.getLogger(this.getClass());
	
    static public final String NL = System.getProperty("line.separator");

    private Cell cell;

    private boolean editable;

    // reference to the main window,
    // IMPORTANT: this must not be null if editable is true
    private BlockCellSetPanel parent=null;

    private boolean showCandidates;
    
    private boolean highlight;
    private boolean warn;
	private boolean patternHighlight;

    static public int [] FULL_CANDIDATES = {1,2,3,4,5,6,7,8,9};
    
	/** Creates new form CellPanel */
    public CellPanel() {

        initComponents();

        customInitComponents();

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        valueTextArea = new javax.swing.JTextArea();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(191, 191, 191)));
        setFocusable(false);
        setMinimumSize(new java.awt.Dimension(51, 51));
        setPreferredSize(new java.awt.Dimension(51, 51));
        setRequestFocusEnabled(false);

        valueTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                valueTextAreaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                valueTextAreaMouseExited(evt);
            }
        });
        add(valueTextArea);
    }// </editor-fold>//GEN-END:initComponents

    private void valueTextAreaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_valueTextAreaMouseEntered
    	if (editable) {
            valueTextArea.requestFocusInWindow();
            valueTextArea.setCaretPosition(0);
        }
    }//GEN-LAST:event_valueTextAreaMouseEntered

    private void valueTextAreaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_valueTextAreaMouseExited
        if (!editable)
            return;

        // ask parent to take over focus, so that the cell can be de-focused
        parent.takeFocus();

        updateCellFromText();
    }//GEN-LAST:event_valueTextAreaMouseExited

    private void updateCellFromText() {
    	ArrayList<Integer> numbers = parseInput();

        if (numbers.size()==0) {
        	// nothing entered
        	
        	if (showCandidates)	{	// it is illegal to enter nothing when showing candidate
        		paintCell();
        		return;
        	}

        	if (!cell.isKnown()) {
        		// no change
        		paintCell();
        		return;
        	}
        }
        
        // check whether changes are made
        if (isEqual(cell, numbers)) {
            logger.debug("no change");
            paintCell();
            return;
        }

        // change the cell
        if (numbers.size() == 0) {
            parent.reset(cell.getRow(), cell.getCol());
        } else if (numbers.size() == 1) {
            parent.setValue(cell.getRow(), cell.getCol(), numbers.get(0));
        } else {
            if (showCandidates) {
                parent.reset(cell.getRow(), cell.getCol());
                cell.setCandidates(numbers);
            } else {
                // when not showing candidates, does not allow edit of candidates
            	paintCell();
            	return;
            }
        }

        paintCell();
        
//        warn = parent.boardChanged();
        parent.boardChanged();

//        repaint();
    }

    private void customInitComponents() {
    	
    	valueTextArea.setEditable(false);
    }

    // to differenciate system triggered or application triggered repaint events
//    boolean appTriggeredRepaint = false;

//    @Override
//    public void paint(Graphics g) {
//    	
//        if (!appTriggeredRepaint) {
//            super.paint(g);
//            return;
//        }
//
//        appTriggeredRepaint = false;
//        
//        setTextBackgroundColor();
//
//        paintCell();
//
//        super.paint(g);
//    }

    private void paintCell() {
    	if (cell==null) {
    		valueTextArea.setBackground(Color.WHITE);
    		valueTextArea.setText("");
//    		super.paint(g);
    		return;
    	}

    	if (cell.isKnown() || !showCandidates) {
    		FlowLayout flowLayout = (FlowLayout) getLayout();
    		flowLayout.setHgap(1);
    		flowLayout.setVgap(1);

                valueTextArea.setColumns(1);
                valueTextArea.setRows(1);
                
    		valueTextArea.setFont(new Font(Font.DIALOG, Font.BOLD, 28));
    		valueTextArea.setForeground(Color.BLACK);

    		if (cell.isKnown()) {
    			//            System.out.println("single value: " + cell.getValue());
    			valueTextArea.setText(Integer.toString(cell.getValue()));
    		} else {
    			valueTextArea.setText(" ");
    		}

    	} else {
    		FlowLayout flowLayout = (FlowLayout) getLayout();
    		flowLayout.setHgap(0);
    		flowLayout.setVgap(0);

                valueTextArea.setColumns(5);
                valueTextArea.setRows(3);

    		valueTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
    		valueTextArea.setForeground(Color.GRAY);

    		valueTextArea.setText("");

    		// unknown value
    		for (int i = 1; i <= 9; i++) {
    			if (cell.getCandidates().contains(i)) {
    				valueTextArea.append(Integer.toString(i));
    			} else {
    				valueTextArea.append(" ");
    			}

    			if (i % 3 != 0)
                            valueTextArea.append(" ");
                        else {
                            if (i!=9)
    				valueTextArea.append(NL);
                        }
    		}
    	}
    }
    	
//    @Override
//    public void repaint() {
//        appTriggeredRepaint = true;
//        super.repaint();
//    }

    public void setHighlight(boolean highLight) {
        this.highlight = highLight;
        setTextBackgroundColor();
    }

    public void setPatternHighlight(boolean patternHighlight) {
        this.patternHighlight = patternHighlight;
       	setTextBackgroundColor();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea valueTextArea;
    // End of variables declaration//GEN-END:variables


    /////////////////////////////////

    public void setCell(Cell cell) {
        this.cell = cell;
        resetHighlighting();
        paintCell();
    }

    public void setShowCandidates(boolean showCandidates) {
        this.showCandidates = showCandidates;
        paintCell();
    }

    public void setEditable(boolean editable) {
		this.editable = editable;
    	valueTextArea.setEditable(editable);
    }

    private boolean isEqual(Cell cell, ArrayList<Integer> numbers) {
        if (cell.isKnown()) {
            if (numbers.size()!=1)
                return false;
            if (cell.getValue()==numbers.get(0))
                return true;
            else
                return false;
        } else {
            if (cell.getCandidates().size() == numbers.size()) {
                if (cell.getCandidates().containsAll(numbers))
                    return true;
            }
            return false;
        }
    }

    public void setParent(BlockCellSetPanel parent) {
        this.parent = parent;
    }

    private void setTextBackgroundColor() {
    	int r=255;
    	int g=255;
    	int b=255;
    	
        if (warn) {
        	g-=64;
        	b-=64;
        }
        if (highlight) {
        	r-=64;
        	g-=64;
        }
        if (patternHighlight) {
        	r-=64;
        	b-=64;
        }
        valueTextArea.setBackground(new Color(r,g,b));
    }

    private ArrayList<Integer> parseInput() {
        // parse input
    	logger.debug("valueArea text: " + valueTextArea.getText());
        Scanner scanner = new Scanner(valueTextArea.getText());
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        while (scanner.hasNext()) {
            String str = scanner.next();
            char [] chars = str.toCharArray();
            for (int idx=0; idx<chars.length; idx++) {
                if (chars[idx]>='0' && chars[idx]<='9') {
                    int i = chars[idx]-'0';
                    
                    // do not add duplicate numbers
                    if (!numbers.contains(i))
                    	numbers.add(i);
                }
            }
        }
        logger.debug("numbers: " + numbers.size());
        return numbers;
    }

	public void setWarn(boolean warn) {
		this.warn = warn;
        setTextBackgroundColor();
	}

	public void resetHighlighting() {
        highlight = false;
        warn = false;
        patternHighlight = false;
        setTextBackgroundColor();
	}


}
