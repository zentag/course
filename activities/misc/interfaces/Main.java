public class Main {
        // try changing PersonIOZen to PersonIONapolean
        public PersonIO myPerson = new PersonIOZen();
        public void main() {
                // this is gauranteed to work since the PersonIO interface requires a greet() method!
                myPerson.greet();
        }
}

