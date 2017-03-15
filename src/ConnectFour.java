import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class ConnectFour implements Cloneable{

    private char[][] board;
    private String lastMove;
    private int value;

    private long waitTime;
    private static final Scanner kb = new Scanner(System.in);
    private boolean run;
    private boolean isUserOnTheBoard;
    private boolean isCpuMove;
    
    public ConnectFour() {
        this.board = new char[8][8];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = '-';
            }
        }
    }

    //this constructor makes it easier to mock
    //a ConnectFour object with a specific value,
    //namely Integer.MAX_VALUE and Integer.MIN_VALUE
    public ConnectFour(int value){
        board = null;
        lastMove = null;
        this.value = value;
    }

    //copy constructor
    public ConnectFour(ConnectFour that){
        this.board = that.getBoard();
        this.lastMove = that.getLastMove();
        this.value = that.getValue();
    }

    //must deep clone
    public char[][] getBoard() {
        char[][] copy = new char[board.length][];
        for (int r = 0; r < copy.length; r++) {
            copy[r] = board[r].clone();
        }
        return copy;
    }

    public long getWaitTime() {
        return waitTime;
    }
    
    public void setWaitTime(long newWaitTime) {
        waitTime = newWaitTime;
    }

    public int getValue() {
        return value;
    }

    public String getLastMove() {
        return lastMove;
    }

    /* public ConnectFour() {
        this.board = new char[8][8];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = '-';
            }
        }
    }

    @Override
    public Object clone(){
        try{
            return super.clone();
        }
        catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return null;
    }
    
    public long getWaitTime() {
        return waitTime;
    }
    
    public void setWaitTime(long newWaitTime) {
        waitTime = newWaitTime;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getLastMove() {
        return lastMove;
    }
    
    public char[][] getBoard() {
        return board;
    } */

    public Set<String> getSuccessors(){
        Set<String> successors = new HashSet<>();
        for (int r = 0; r < this.getBoard().length; r++) {
            for (int c = 0; c < this.getBoard().length; c++) {
                if (this.getBoard()[r][c] == '-'){
                    this.getBoard()[r][c] = 'X';
                    char row = (char) (r+65);
                    int col = c+1;
                    String result = row+(col+"");
                    successors.add(result);
//                    this.getBoard()[r][c] = '-';
                }

            }
        }
        return successors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        for (int i = 1; i <= board.length; i++) {
            sb.append(" " + i);
        }
        sb.append("\n");
        for (int i = 0; i < board.length; i++) {
            sb.append((char) (i + 'A'));
            for (int j = 0; j < board.length; j++) {
                sb.append(" " + board[i][j]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    //sum of adjacent Os - sum of adjacent Xs
    //probably too complicated
    public int evaluation(ConnectFour cf){
        int result = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char token = cf.getBoard()[r][c];
                if (token == '-')
                    continue; // don't check empty slots

                int horizontalCounter = 1;
                while (c + horizontalCounter < 8 &&
                       token == this.getBoard()[r][c+horizontalCounter]) { //horizontal right
                            result += (token == 'X')? 1 : -1;
                            horizontalCounter++;// no winner found
                }

                int verticalCounter = 1;
                while (r + verticalCounter < 8 &&
                       token == this.getBoard()[r+verticalCounter][c]) { //vertical down
                            result += (token == 'X')? 1 : -1;
                            verticalCounter++;
                }
            }
        }
        return result;
    }

    private boolean userPlayRound() {
        String move = "";
        do {
            System.out.print("Enter your move: ");
            move = kb.nextLine().toUpperCase();
            if (!isValidMove(move)) {
                System.out.println("Invalid move or space taken. Try again.");
            }
        } while (!isValidMove(move));
        int row = (int) move.charAt(0) - 65;
        int col = Integer.parseInt(move.charAt(1) + "");
        this.placeToken(row, col - 1, 'O');
        isUserOnTheBoard = true;
        System.out.println(this);
        if (this.hasWinner('O')) {
            System.out.println("User wins!");
            return true;
        }
        return false;
    }

    private String cpuMakeMove(ConnectFour cf) {
        //keep track of latest best move from iterative deepening
        ArrayList<String> bestMoves = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                run = true;
                while (run) {
                    //the alpha-beta pruning with minimax call goes here inside the add function
                    chooseMove(cf);
                    if(run == false)
                       break;
                }
                synchronized(this) {
                    this.notify();
                }
            }
        }
        );
        t.start();
        try {
            synchronized (this) {
                this.wait(this.getWaitTime());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        run = false;
        //System.out.println("Size of the bestMoves: " + bestMoves.size());
        //String bestMove = bestMoves.get(bestMoves.size() - 1);
        String bestMove = lastMove;
        return bestMove;
    }

    private boolean cpuPlayRound() {
        isCpuMove = true;
        System.out.print("CPU move is: ");
        ConnectFour cf = (ConnectFour)this;
        String move = cpuMakeMove(cf);
        System.out.println(move);
        int row = (int) move.charAt(0) - 65;
        int col = Integer.parseInt(String.valueOf(move.charAt(1)));
        cf.placeToken(row, (col - 1), 'X');
        System.out.println(cf);
        if (this.hasWinner('X')) {
            System.out.println("CPU wins!");
            return true;
        }
        isCpuMove = false;
        return false;
    }

    //used for testing the timing functionality of the cpuMakeMove
    private String generateRandomMove() {
        Random r = new Random();
        char row = (char) (r.nextInt(8) + 'A');
        int col = r.nextInt(8) + 1;
        String result = (row + "") + (col + "");
        return result;
    }
    
    private String generateRandomOptimalMove() {
        Random r = new Random();
        char row = (char) (r.nextInt(1) + 'D');
        int col = r.nextInt(1) + 4;
        String result = (row + "") + (col + "");
        return result;
    }

    private String aBPruning() {
        ConnectFour cf = (ConnectFour) this;
        cf.search(cf, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return cf.getLastMove();
    }

    private void chooseMove(ConnectFour cf) {
        int maxScore = Integer.MIN_VALUE;
        String bestMove = null;
        
        Set<String> moves = cf.getSuccessors();

        for (String move : moves) {

            ConnectFour newState = (ConnectFour) cf;
            
            newState.makeMove(move, 'X');
            //System.out.println(newState);
            int score = iterativeDeepeningSearch(newState);
//            newState.makeMove(move, '-');
            
            if (score >= Integer.MAX_VALUE) { //winner found
                lastMove = move;
            }

            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
                lastMove = bestMove;
            }
        }
    }
    
    private int iterativeDeepeningSearch(ConnectFour cf) {
        int depth = 1;
        int score = 0;

        int searchResult = search(cf, depth, Integer.MIN_VALUE, Integer.MAX_VALUE);

        if (searchResult >= Integer.MAX_VALUE) { //found a winning move
            return Integer.MAX_VALUE;
        } else {
            score = searchResult;
        }
        depth++;

        return score;
    }
    
    private int search(ConnectFour cf, int depth, int alpha, int beta) {
        Set<String> moves = cf.getSuccessors();
        int winningScore = isCpuMove ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int score = cf.evaluation(cf);
        
        // If this is a terminal node return score with evaluation function
        if (depth == 0|| (moves.size() == 0)) {
            return score;
        //if it is a win, abort the search
        } else if(hasWinner('X') || hasWinner('O')) {
            return winningScore;
        }
        
        if (isCpuMove) {
                for (String move : moves) {
                        ConnectFour childcf = (ConnectFour)cf;
                        childcf.makeMove(move, 'X');
                        alpha = Math.max(alpha, search(childcf, depth - 1, alpha, beta));
                        
                        if (beta <= alpha) {
                                break;
                        }
//                        childcf.makeMove(move, '-');
                }
                isCpuMove=false;
                return alpha;
        } else {
                for (String move : moves) {
                        ConnectFour childcf = (ConnectFour)cf;
                        childcf.makeMove(move, 'O');
                        
                        beta = Math.min(beta, search(childcf, depth - 1, alpha, beta));
                                
                        if (beta <= alpha) {
                                break;
                        }
//                        childcf.makeMove(move, '-');
                }
                isCpuMove=true;
                return beta;
        }
    }


    private boolean isValidMove(String move) {
        if (move.length() != 2 || !Character.isLetter(move.charAt(0))) {
            return false;
        }
        int firstChar = (int)move.charAt(0)-64;
        int secondChar;
        try {
            secondChar = Integer.parseInt(move.charAt(1) + "");
        } catch (NumberFormatException nfe) {
            return false;
        }
        return  (firstChar >= 1 && firstChar <= 8) &&      //first char is in A-H
                (secondChar >= 1 && secondChar <= 8) &&    //second char is 1-8
                (board[firstChar-1][secondChar-1] == '-'); //space is open on board 
    }
    
    public boolean hasWinner(char playerToken) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char token = board[r][c];
                if (token == '-' || token != playerToken)
                    continue; // don't check empty slots
    
                if (c + 3 < 8 &&
                    token == board[r][c+1] && //horizontal
                    token == board[r][c+2] &&
                    token == board[r][c+3])
                    return true;
                if (r - 3 > 0 &&
                    token == board[r-1][c] && //vertical
                    token == board[r-2][c] &&
                    token == board[r-3][c])
                    return true;
    
                }
            }
        return false; // no winner found
    }

    private void placeToken(int r, int c, char token) {
        this.board[r][c] = token;
        lastMove = (char)(r + 'A') + String.valueOf(c + 1);
    }
    
    private void makeMove(String move, char token) {
        int row = (int) (move.charAt(0)-65);
        int col = Integer.parseInt(move.charAt(1)+"");
        col -= 1;
        this.getBoard()[row][col] = token;
    }

    public static void main(String[] args) {
        System.out.println("How much time (in seconds) will you allow the computer to generate an answer?");
        long seconds;
        do {
            System.out.print("\tTime: ");
            seconds = kb.nextLong();
            kb.nextLine();
            if (seconds > 30) {
                System.out.println("The maximum time allowed is 30 seconds. Please enter another time.");
            }
        } while (seconds > 30);
        
        System.out.println("And who should go first?");
        String firstMove = "";
        boolean validChoice = false;
        do {
            System.out.print("\tCPU or USER? ");
            firstMove = kb.nextLine().toUpperCase();
            validChoice = firstMove.equals("USER") || firstMove.equals("CPU");
            if (!validChoice) {
                System.out.println("Not a valid choice. Please enter CPU or USER.");
            }
        } while (!validChoice);
        ConnectFour cf = new ConnectFour();
        System.out.println("");
        seconds *= 1000;
        cf.setWaitTime(seconds);
        System.out.println("Initial board:");
        System.out.println(cf);

        //unraveled cyclic method calls for fear of stack overflow
        while ( !cf.hasWinner('X') ||  !cf.hasWinner('O')) {
            if (firstMove.equals("USER")) {
                if ( cf.userPlayRound() )
                    break;
                if ( cf.cpuPlayRound() )
                    break;
            } else {
                if ( cf.cpuPlayRound() )
                    break;
                if ( cf.userPlayRound() )
                    break;
            }
        }

    }

}
