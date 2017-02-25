public class ConnectFour {

    private char[][] board;

    public static void main(String[] args){
        System.out.println(new ConnectFour());
    }

    public ConnectFour() {
        this.board = new char[8][8];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = '-';
            }
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        for (int i = 1; i <= board.length ; i++) {
            sb.append(" " + i);
        }
        sb.append("\n");
        for (int i = 0; i < board.length; i++) {
            sb.append((char)(i + 65));
            for (int j = 0; j < board.length; j++) {
                sb.append(" " + board[i][j]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
