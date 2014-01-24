# Humans vs Zombies Path Planner

A path planner that takes risk into account in planning routes through the
Georgia Tech campus for Humans vs Zombies. This is still very much a
work-in-progress.

This was a project for the class [Robot Intelligence:
Planning](http://www.cc.gatech.edu/~mstilman/class/RIP13/), offered at
Georgia Tech in Fall 2013. (See the final paper at
[docs/paper.pdf](docs/paper.pdf).)

## Eclipse Setup

Clone the repository and add it to your workspace. Install the [Maven Eclipse
plugin](http://maven.apache.org/eclipse-plugin.html), and right-click the
project and go to `Configure`/`Convert to Maven project`. The dependencies
should work automatically then.

## Running

Currently there are two classes with `main` methods:
`zombieplanner.simulator.ZombieSimulator` and
`zombieplanner.simulator.ZombieSimulatorUI`. The former runs an experiment we
developed for the project that generates random start and end locations to test.
The latter runs the UI for the user, which is what you'll generally want to do
to demo the application.

Please submit issues or pull requests if you have anything to improve! There is
a lot of work that needs to be done on organizing the code (as most of it was
written the night before the project was due). Thanks.

