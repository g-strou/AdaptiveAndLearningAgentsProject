package alas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;


@SuppressWarnings("serial")
public class AlasFrame extends JFrame{
	
	public static final int FRAME_WIDTH = 1000;
	public static final int FRAME_HEIGHT = 500;
	public static final int GRID_WIDTH = 500;
	
	public static final int STATIC_AGENTS_NUMBER = 36;
	public static final int REWARD_INDEX = 4;

	private int [][] grid;
	private String [][] allStaticAgents; 
	private Agent agent;
	private Timer timer = null;
	private int delay = 500;
	private int forayCounter;
	
	private JPanel gridPanel;
	private JTextArea txtArea;
	private JComboBox extraEnergyBox;
	private JButton forayButton;
	private JButton resetButton;
	private JButton extractButton;
	private JButton searchPathsButton;
	
	
	public AlasFrame() {
		
		super("ALAS");
		grid = readGrid("training-env.txt", 6);
		allStaticAgents = readStaticAgents("Table1.txt");
		setupFrame();
		reset();
	}
	
	// This method returns all the combinations of UP and RIGHT directions that lead from the 
	// SW tile of the test environment to the NE tile. 
	// These are all the possible 6-length paths to be considered.
	public static ArrayList<int []> findAllPossiblePaths() {
		
		ArrayList<String> combinations = new ArrayList<String>();
		ArrayList<int []> paths = new ArrayList<int []>();
		
		for(int a=0; a<=3; a++)
			for(int b=a+1; b<=4; b++)
				for(int c=b+1; c<=5; c++) {
					combinations.add(new String("" + a + b + c));
					int[] path = new int[6]; //initialization to 0
					path[a] = path[b] = path[c] = 1;
					paths.add(path);
				}
		return paths;
	}
	
	// This static method accepts a SA with unknown reward attribute and returns the predicted value.
	// This method is called for the last question, the path search, after the learning stage has been done
	// and a decision rule has been extracted
	public static int decisionTreeForReward(String [] staticAgent) {
		
		String shape = staticAgent[0];
		//String color = staticAgent[1];
		String size = staticAgent[2];
		String shadow = staticAgent[3];
		
		//here we implement the decision tree
		if (shape.equals("diamond")) {
			if (size.equals("small")) 
				return 0;
			else {		//large
				if (shadow.equals("yes"))
					return -5;
				else
					return 0;
			}
		}
		else if (shape.equals("square")){
			if (size.equals("small")) 
				return -1;
			else		//large
				return +2;
		}
		else { //circle
			return 0;
		}
	}
	
	// reset
	public void reset() {
		grid = readGrid("training-env.txt", 6);
		agent = new Agent(grid, allStaticAgents);	
		txtArea.setText("");
		forayCounter = 0;
		gridPanel.repaint();
		setButtonsEnabled(true);
	}
	
	// This function starts a new foray. It accepts the extra energy parameter as argument 
	public void foray(int extraEnergy) {
		
		setButtonsEnabled(false);
		
		forayCounter++;		//counter
		
		agent.prepareForay(22, extraEnergy);		//call prepare foray method of the agent
		
		// this method also prints the output
		txtArea.append("------------------------ \n");
		txtArea.append("NEW FORAY \tcounter= " + forayCounter + "\tExtra Energy: " + extraEnergy +'\n');
	
		// the timer
		timer = new Timer(delay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				agent.step();		// the next step of the agent
				
				gridPanel.repaint();
				
				// if the energy gets below 1, then the foray is unsuccessful
				if(agent.getEnergy() < 1 ) {
					exitForay(-1);
				}	//else we check if the agent reached the SW tile
				else if ((agent.getRow() == 0 && agent.getColumn() == 0)) {
					exitForay(1);
				} 

			}
		});
		
		timer.start();
		
	}
	
	// This function is called at the end of each foray. 
	// If the foray is successful, the successFlag is 1, otherwise -1.
	// If the foray is successful, the agent's knowledge base is updated to inlcude
	// any new SA collected. 
	// The rest of the code is to print the output.
	 public void exitForay(int successFlag){
	     
		 setButtonsEnabled(true);
		 timer.stop();
	     
		 String line = "";
		 
		 if (successFlag <0) {
			 txtArea.append("-- UNSUCCESFUL FORAY -- ");
		     for (int [] tile : agent.getPath()) {
		    	
		    	 line += "-(" + tile[0] + "," + tile[1] + ")";
		     }	
			 txtArea.append(line + "\n------------------------ \n");
			 return;
		 }
		 
	     agent.updateKnowledgeBase();		// update the knowledge base
	     
	     line = "\nPath: ";
	     for (int [] tile : agent.getPath()) {
	    	 line += "-(" + tile[0] + "," + tile[1] + ")";
	     }	
	     line += "\n";
		
	     txtArea.append(line);
		
	     line = "AgentsVisited: ";
	     for (Integer id : agent.getCollectedSA()) {
	    	 line += " - " + id ;
	     }
	     line += "\n";
		
	     txtArea.append(line);
	     
	     txtArea.append("L: " + agent.getCollectedSA().size() + "\n");
	     
	     line = "SA_KB: ";
	     
	     ArrayList<Integer> sortedList = agent.getAllKnownSA();
	     Collections.sort(sortedList);
	     
	     for (Integer id : sortedList) {
	    	 line += " - " + id ;
	     }
	     line += "\nTotally known SA: " + sortedList.size() + "\n";
	     
	     txtArea.append(line);
	     
	    
	 }
	
	// This function examines all the possible paths of question 4 and accepts the initial energy as argument 
	// It calls the searchTestEnvironment method of the agent.
	 public void findPaths(int [] initEnergyValues) {
		
		setButtonsEnabled(false);
		resetButton.setEnabled(true);
		 
		grid = readGrid("test-env.txt", 4);		
		gridPanel.repaint();
		agent = new Agent(grid, allStaticAgents);
		
		ArrayList<int []> allPaths = findAllPossiblePaths();
		
		txtArea.setText("");
		
		for (int initialEnergy : initEnergyValues) {
			
			ArrayList<String> succPaths =  agent.searchTestEnvironment(allPaths, initialEnergy);
			
			txtArea.append(" ---- SEARCH FOR PATHS IN TEST ENVIRONMENT ---- initial energy: " + initialEnergy + "\n");
			for (String s: succPaths) txtArea.append(s + "\n");
			txtArea.append("Totally: " +  succPaths.size());
			txtArea.append("-------------------------------------------------------\n\n");
		}
	}
	

	
	// ****************** the rest of the code is for GUI and input-output *********
	
	// ************************************************************************************
	// ************************************ gui made here **********************************
	 public void setupFrame() {
			
			JPanel topPanel = makeTopPanel();
			gridPanel = makeEnvironmentPanel();
			
			JPanel controlPanel = new JPanel();
			controlPanel.add(topPanel);

			
			GridLayout gridLayout = new GridLayout(1,2);
			this.setLayout(gridLayout);
			this.add(controlPanel);
			this.add(gridPanel);
			

		}
	 
	public JPanel makeTopPanel() {

		JPanel panel1 = new JPanel();

		extraEnergyBox =  new JComboBox(new Integer[]{1, 2, 3, 4, 5});
		extraEnergyBox.setSelectedIndex(1);
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				reset();
			}
		});
		panel1.add(resetButton);
		
		forayButton = new JButton("Foray");
		forayButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				foray((Integer)extraEnergyBox.getSelectedItem());
			}
		});
		panel1.add(forayButton);
		
		
		extractButton = new JButton("Extract Data");
		extractButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				extractARFF("data.arff", agent.getAllKnownSA(), allStaticAgents);
			}
		});
		panel1.add(extractButton);
		
		
		panel1.add(new JLabel("Extra Energy"));
		panel1.add(extraEnergyBox);
		
		JPanel retPanel = new JPanel();
		retPanel.setLayout(new BoxLayout(retPanel, BoxLayout.PAGE_AXIS));
		
		txtArea = new JTextArea(20, 40);
		JScrollPane scrollPane = new JScrollPane(txtArea);
		
		JPanel panel2 = new JPanel();

		searchPathsButton = new JButton("Test - Search 6_length Paths");
		searchPathsButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				findPaths(new int[]{6, 8, 11});
			}
		});
		panel2.add(searchPathsButton);
		
		retPanel.add(panel1);
		retPanel.add(scrollPane); 
		retPanel.add(panel2); 
		
		return retPanel;
	}
	
	public void setButtonsEnabled(boolean b) {
		forayButton.setEnabled(b);
		resetButton.setEnabled(b);
		extractButton.setEnabled(b);
		searchPathsButton.setEnabled(false);
	}
	
	public JPanel makeEnvironmentPanel() {
		return new JPanel() {
			public void paintComponent(Graphics gr) {
				Graphics2D g = (Graphics2D) gr;
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				
				int row, col;
				
				if (grid != null) {
					
					int length = grid.length;
					int cellSize = getHeight() / length;
					
					if (agent!=null && grid.length >5) {	//we draw the agent only in the training environment
						
						g.setColor(Color.yellow);
						
						for (row=0; row < length; row++) {
							for(col=0; col< length; col++) {	
								
								if (agent.hasVisited(row, col)) {
									g.fillRect(col*cellSize, (5-row)*cellSize, cellSize, cellSize);	
								}
							}
						}
						
						row = agent.getRow();
						col = agent.getColumn();
						g.setColor(Color.red);
						g.fillRect(col*cellSize, (5-row)*cellSize, cellSize, cellSize);	
						
					}					

					g.setColor(Color.black);

					
					for (row=0; row < length; row++) {
						for(col=0; col< length; col++) {
							g.drawRect(col*cellSize, row*cellSize, cellSize, cellSize);		
							
							if (row !=  agent.getRow() || col != agent.getColumn() || grid.length < 5) 
								g.drawString("" + grid[row][col], col*cellSize + 30, getHeight() - row*cellSize - 30);
						}
					}
					
					if (grid.length > 5) {
						Font currentFont = g.getFont();
						g.setFont(new Font("TimesRoman", Font.BOLD, 24));
						g.setColor(Color.YELLOW);
						g.drawString("" +  agent.getEnergy(), agent.getColumn()*cellSize + 30, getHeight() - agent.getRow()*cellSize - 30);
						g.setFont(currentFont);
					}
					
				}
					
			}

		};
	}
	 
	 // ************************************************************************************
	// ************************************ static methods for input-output ****************
	
	//extracts the arff file for WEKA
	public static void extractARFF(String filename, ArrayList<Integer> collectedSA, String [][] allStaticAgents) {
		
		try {
			File file = new File(filename);
			PrintWriter printWriter = new PrintWriter(file);

			printWriter.println("@relation rewardRelation");

			printWriter.println("@attribute shape{diamond, square, circle}");
			printWriter.println("@attribute colour{pink, blue, yellow}");
			printWriter.println("@attribute size{small, large}");
			printWriter.println("@attribute shadow{yes, no}");
			printWriter.println("@attribute reward{zero, minus_5, minus_1, plus_2}");
			
			printWriter.println("@data");
			
		    for (Integer id : collectedSA) {
		    	 
		    	String [] sa = allStaticAgents[id];
		    	 for(int i=0; i< sa.length-1; i++) {
		    		printWriter.print(sa[i] + ",");
		    	 }
		    	 
		    	 int reward = Integer.valueOf(sa[sa.length-1]);
		    	 switch (reward) {
		    	 	case 0: 
		    	 		printWriter.print("zero");
		    	 		break;
		    	 	case -1: 
		    	 		printWriter.print("minus_1");
		    	 		break;
		    	 	case -5: 
		    	 		printWriter.print("minus_5");
		    	 		break;
		    	 	case 2: 
		    	 		printWriter.print("plus_2");
		    	 		break;
		    	 	default:
		    	 		break;
		    	 }
		    	 
		    	 printWriter.println();
		    }
			
			printWriter.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

	}

	// reads the 'Table1.txt' file
	public static String [][] readStaticAgents(String fileName) {
		try {
            File file = new File(fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);	
		
            String [][] staticAgents = new String[STATIC_AGENTS_NUMBER+1][REWARD_INDEX];
            
            staticAgents[0] = null; //extra element at zero so that the array index 
            						//of each static agent is equal to its id
            reader.readLine();
            
            for (int i=1; i<=STATIC_AGENTS_NUMBER;i++) {
                String line = reader.readLine();
                String [] stAgent = line.substring(line.indexOf("[") + 1, line.indexOf("]")).split(",");
                staticAgents[i] = stAgent;
            }
            
            reader.close();
            return staticAgents;
        }
        catch(Exception ex) {
            ex.printStackTrace(); 
            return null;
        } 
	}
	
	
	public static void printStaticAgentsToFile(String [][] data, String fileName) {
		try {
			File file = new File(fileName + ".txt");
			PrintWriter printWriter = new PrintWriter(file);
				
			for (int i=1; i<data.length; i++) {
				String [] attributes = data[i];
				
				for (String s: attributes) {
					printWriter.print(s);
					printWriter.println();
				}
			}
			
			printWriter.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	// reads a grid from a filename
	public static int [][] readGrid(String fileName, int size) {
		try {

            File file = new File(fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            
            int [][] grid = new int[size][size];
            
            for (int i=size-1; i>=0 ;i--) {
                String[] fields = reader.readLine().split(" ");
            	for (int j=0; j<size;j++) 
            		grid[i][j] = Integer.valueOf(fields[j]);
            }
            reader.close();
            return grid;
        }
        catch(Exception ex) {
            ex.printStackTrace(); 
            return null;
        } 
	}
	
	// ************************************************************************************
	// ************************************ main **********************************
	
	public static void main(String[] args) {
		
		AlasFrame alasFrame = new AlasFrame();	
		
		alasFrame.setSize(FRAME_WIDTH  ,FRAME_HEIGHT);
		alasFrame.setVisible(true);
		alasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
