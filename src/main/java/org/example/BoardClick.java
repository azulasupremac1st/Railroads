package org.example;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
public class BoardClick extends MouseAdapter{
    private final BoardPanel board;

    public BoardClick(BoardPanel board){
        this.board = board;
    }

    public void mouseClicked(MouseEvent e){
        int mouseX = e.getX();
        int mouseY = e.getY();


        int gridX = mouseX - Configurations.GRID_PADDING;
        int gridY = mouseY - Configurations.GRID_PADDING;

        if(gridX<0 || gridY<0){return;}

        int gridWidth = board.getColumnCount() * Configurations.TILE_SIZE;
        int gridHeight = board.getRowCount() * Configurations.TILE_SIZE;

        if(gridX >= gridWidth || gridY >= gridHeight){return;}

        int col = gridX/Configurations.TILE_SIZE;
        int row = gridY/Configurations.TILE_SIZE;

        //System.out.println("mouse clicked at" + mouseX + " , "+mouseY);
        board.onTileClicked(row, col);
    }

}
