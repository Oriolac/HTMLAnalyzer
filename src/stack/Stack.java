package stack;

public interface Stack<E> {
        Stack<E> push(E elem);
        Stack<E> pop() throws StackError;
        E top() throws StackError;
        boolean isEmpty();
}
