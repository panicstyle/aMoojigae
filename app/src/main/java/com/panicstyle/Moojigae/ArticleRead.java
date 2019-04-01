package com.panicstyle.Moojigae;

public class ArticleRead {
    private String id;
    private String board;

    public ArticleRead() {
        this("", "");
    }

    public ArticleRead(String _id) {
        this(_id, "");
    }

    public ArticleRead(String _id, String _board) {
        this.id = _id;
        this.board = _board;
    }

    public String getId() {
        return id;
    }

    public void setId(String _id) {
        this.id = _id;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String _board) {
        this.board = _board;
    }
}
