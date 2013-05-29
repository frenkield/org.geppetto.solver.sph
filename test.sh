mvn compile
java -cp "target/classes/lib/*":target/classes:target/test-classes:/usr/share/java/junit4.jar org.junit.runner.JUnitCore org.geppetto.solver.sph.internal.PCISPHSolverTest

