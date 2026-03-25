public record Tree(String type, int age, String location) {
    public void hideShorty(String name) {
        System.out.println(name +" скрывается за " + type + " в " + location + ".");
    }
}
