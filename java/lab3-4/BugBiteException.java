public class BugBiteException extends Exception {
        @Override
        public String getMessage() {
            return "Насекомое не смогло укусить!";
        }
}
