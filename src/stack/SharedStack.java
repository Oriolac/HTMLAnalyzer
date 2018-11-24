package stack;

import java.util.Iterator;
import java.util.List;

public class SharedStack<E> implements Stack<E>{
    //Atributs
    private final Node<E> topOfStack;

    //Subclasse Node<E>
    private static class Node<E>{

        private final E elem;
        private final Node<E> next;

        private Node(E elem, Node<E> next){
            this.elem = elem;
            this.next = next;
        }

    }
    //Constructors

    public SharedStack(){
        topOfStack = null;
    }

    private SharedStack(Node<E> topOfStack){
        this.topOfStack = topOfStack;
    }


    // Mètode estàtic
    public static <E> SharedStack<E> stackFromList(List<E> list){
        Iterator<E> it = list.iterator();
        SharedStack<E> stack = new SharedStack<>();
        while(it.hasNext()){
            E elem = it.next();
            stack = stack.push(elem);
        }
        return stack;
    }

    //Mètodes de stack.Stack<E>
    @Override
    public SharedStack<E> push(E elem) {
        return new SharedStack<E>(new Node<E>(elem, this.topOfStack));
    }

    @Override
    public SharedStack<E> pop() throws StackError {
        if(isEmpty()) throw new StackError("Error pila buida.");
        return new SharedStack<>(topOfStack.next);
    }

    @Override
    public E top() throws StackError {
        if(isEmpty()) throw new StackError("Error pila buida");
        return topOfStack.elem;
    }

    @Override
    public boolean isEmpty() {
        return null == topOfStack;
    }

    //Mètodes de comprovació de contingut i d'adreça dels nodes

    public static <E> boolean isSameContent(SharedStack<E> s1, SharedStack<E> s2){
        return s1.contentToString().equals(s2.contentToString());
    }

    public String contentToString(){
        try{
            return "{ " + contentToString(topOfStack) + "}";
        } catch (NullPointerException ex){
            return "Pila buida";
        }
    }


    private String contentToString(Node<E> node){
        if(node.next == null){
            return "" + node.elem;
        }
        return node.elem + ", " + contentToString(node.next);
    }

    public static <E> boolean isSameAdress(SharedStack<E> s1, SharedStack<E> s2){
        return s1.adressToString().equals(s2.adressToString());
    }

    public String adressToString() {
        try{
            return "{ " + adressToString(topOfStack) + "}";
        } catch (NullPointerException ex){
            return "Pila buida";
        }
    }

    private String adressToString(Node<E> node) {
        if(node.next == null){
            return "@" + Integer.toHexString(node.hashCode());
        }
        return "@" + node.hashCode() + ", " + adressToString(node.next);
    }

}
