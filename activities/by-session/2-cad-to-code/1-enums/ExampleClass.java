public class ExampleClass {
        // a list of all the possible values
        public enum RainbowColor {
                RED,
                ORANGE,
                YELLOW,
                GREEN,
                BLUE,
                INDIGO,
                VIOLET
        }
        // myColor must be set to one of the values above
        public RainbowColor myColor = RainbowColor.YELLOW;
        // similar to public ExampleClass() {
        // this runs once, when you start the program
        public void main() {
                printColor();
                myColor = RainbowColor.RED;
                printColor();
        }
        public void printColor() {
                // switch: check the value of myColor, run the corresponding code
                // you will get a warning if there is not a case for every possible value!
                switch(myColor) {
                case RED -> System.out.println("Color is red!");
                case ORANGE -> System.out.println("Color is orange!");
                case YELLOW -> System.out.println("Color is yellow!");
                case GREEN -> System.out.println("Color is green!");
                case BLUE -> System.out.println("Color is blue!");
                case INDIGO -> System.out.println("Color is indigo!");
                case VIOLET -> System.out.println("Color is violet!");
                }
        }
}
