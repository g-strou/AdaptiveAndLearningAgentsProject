This is a simple project I did for the module AdaptiveAndLearningAgents at the University of York.

A moving agent must explore a grid consisting of numbered static agents (cells) and record their properties.
The moving agent has an initial amount of energy and each step costs 1 energy point.
Additionally, each static agents imposes a penalty or a reward to the initial energy of the moving agent.
The properties and energy penalty/reward of each agent are shown in Table1.txt.

The moving agent in order to store the properties of the cells, must return safe to the base e.g. with energy >=1
The agent can perform as many forays as needed. 
The aim of the project is to maximize the total number of cells explored, minimizing the number of unsuccessful forays.

The program:


- RESET button starts a new exploration cycle.
- FORAY starts a new foray. The remaining energy is shown at the red cell, which is the current energy of the agent
- EXTRACT DATA extracts an arff file with the properties of the explored cells that was used as input to Weka for subsequent tasks
- EXTRA ENERGY combobox determines the strategy of the agent. The agent will start returning to the base when its energy is equal
to its distant from the base plus the extra energy. A big value of this setting allows the agent to move further away with an increased
risk of not returning to the base (unsuccesful foray)


The SEARCH PATHS button (disabled now) was used for anoher task.