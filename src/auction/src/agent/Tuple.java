package agent;

public class Tuple <T1,T2>{
    public T1 left;
    public T2 right;

    Tuple(T1 left, T2 right){
        this.left = left;
        this.right = right;
    }

    public void println(){
        System.out.println("<"+this.left+";"+this.right+">");
    }
}