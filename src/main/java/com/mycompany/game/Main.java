package com.mycompany.game;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class Main extends JFrame {

    private GamePanel panelGame = new GamePanel();
    private JPanel buttonsPanel = new JPanel();
    private JButton bStart = new JButton("Start");
    private JButton bReset = new JButton("Reset");
    private Container container = this.getContentPane();

    public static void main(String[] args) {
        new Main().setVisible(true);
    }

    public Main() {
        super("SimpleGame");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage("Game.jpg"));
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setBounds(width / 4, height / 4, width / 2, height / 2);
        initComponent();
    }

    public void initComponent() {
        buttonsPanel.add(bStart);
        buttonsPanel.add(bReset);
        container.add(panelGame, BorderLayout.CENTER);
        container.add(buttonsPanel, BorderLayout.SOUTH);
        panelGame.setBackground(Color.GRAY);
        panelGame.addKeyListener(new Plane());

        bStart.addActionListener(
                (arg0)
                -> {
            startAnimation();
        }
        );

        bReset.addActionListener(
                (arg0)
                -> {
            resetAnimation();
        }
        );
    }

    private void startAnimation() {
        panelGame.requestFocus();
        panelGame.addObstacle();
    }

    private void resetAnimation() {
        panelGame.resetGame();
    }

    class GamePanel extends JPanel {

        public ArrayList listPlane = new ArrayList();
        public ArrayList listObstacle = new ArrayList();
        Thread thread;
        ThreadGroup groupObstacle = new ThreadGroup("Group obstacle");

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (listPlane.isEmpty()) {
                listPlane.add(new Plane());
                ((Plane) listPlane.get(0)).resetPosition(panelGame);
            }

            g.drawImage(Plane.getImagePlane(), ((Plane) listPlane.get(0)).x, ((Plane) listPlane.get(0)).y, null);

            for (int i = 0; i < listObstacle.size(); i++) {
                g.drawImage(Obstacle.getImageBird(), ((Obstacle) listObstacle.get(i)).xObstacle, ((Obstacle) listObstacle.get(i)).yObstacle, null);

                if (((Obstacle) listObstacle.get(listObstacle.size() - 1)).yObstacle > (10000 / Plane.widthPanel) - 10) {
                    addObstacle();
                }
            }

            for (int i = 0; i < listObstacle.size(); i++) {
                if (((Obstacle) listObstacle.get(i)).yObstacle > Plane.heightPanel) {
                    listObstacle.remove(i);
                }

                if (((Obstacle) listObstacle.get(i)).yObstacle + Obstacle.heightObstacle != 0 && ((Plane) listPlane.get(0)).y != 0 && ((Obstacle) listObstacle.get(i)).yObstacle != 0) {
                    boolean conditionOfTruth = (((((Plane) listPlane.get(0)).y) + Plane.yPlane) / ((Obstacle) listObstacle.get(i)).yObstacle >= 1 && ((((Obstacle) listObstacle.get(i)).yObstacle + Obstacle.heightObstacle) / ((Plane) listPlane.get(0)).y) >= 1);
                    boolean conditionVertical = ((((Obstacle) listObstacle.get(i)).yObstacle + Obstacle.heightObstacle) % ((Plane) listPlane.get(0)).y) < Obstacle.heightObstacle + Plane.yPlane;
                    boolean conditionHorizontalLeft = (((((Obstacle) listObstacle.get(i)).xObstacle) + Obstacle.widthObstacle) >= Plane.x) && (Plane.x >= ((Obstacle) listObstacle.get(i)).xObstacle);
                    boolean conditionHorizontalRight = (((((Obstacle) listObstacle.get(i)).xObstacle) + Obstacle.widthObstacle) >= Plane.x + Plane.xPlane) && (Plane.x + Plane.xPlane >= ((Obstacle) listObstacle.get(i)).xObstacle);
                    boolean conditionHorizontalMid = (((((Obstacle) listObstacle.get(i)).xObstacle) + Obstacle.widthObstacle) >= Plane.x + (Plane.xPlane / 2)) && (Plane.x + (Plane.xPlane / 2) >= ((Obstacle) listObstacle.get(i)).xObstacle);
                    boolean accidentLeft = (conditionVertical && conditionHorizontalLeft && conditionOfTruth);
                    boolean accidentRight = (conditionVertical && conditionHorizontalRight && conditionOfTruth);
                    boolean accidentMid = (conditionVertical && conditionHorizontalMid && conditionOfTruth);

                    if (accidentLeft || accidentRight || accidentMid) {
                        this.resetGame();
                        JOptionPane.showConfirmDialog(rootPane, "Game Over", "Game over", -1, 0);
                    }
                }
            }
        }

        private void addObstacle() {
            listObstacle.add(new Obstacle());
            thread = new Thread(groupObstacle, new ObstacleRunnable((Obstacle) listObstacle.get(listObstacle.size() - 1)));
            thread.start();
        }

        private void resetGame() {
            listPlane.clear();
            listObstacle.clear();
            repaint();
        }

        public class ObstacleRunnable implements Runnable {

            Obstacle obstacle;

            public ObstacleRunnable(Obstacle obstacle) {
                this.obstacle = obstacle;
            }

            @Override
            public void run() {
                try {
                    while (true) {
                        this.obstacle.moveObstacle();
                        repaint();
                        Thread.sleep(50);
                    }
                } catch (InterruptedException ex) {
                    System.out.println(ex.getMessage());
                }

            }
        }
    }

}

class Plane extends KeyAdapter {

    protected static int speedPlane = 5;
    protected static int dx = 0;
    protected static int dy = 0;
    protected static Image plane = new ImageIcon("Plane.jpg").getImage();
    protected static int x;
    protected static int y;
    protected static int widthPanel;
    protected static int heightPanel;
    protected static int xPlane = plane.getWidth(null);
    protected static int yPlane = plane.getHeight(null);

    public static Image getImagePlane() {
        return Plane.plane;
    }

    public void resetPosition(JPanel panel) {
        Rectangle boundsPanel = panel.getBounds();
        widthPanel = (int) boundsPanel.getWidth();
        heightPanel = (int) boundsPanel.getHeight();
        x = widthPanel / 2 - xPlane / 2;
        y = heightPanel - yPlane;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && widthPanel - xPlane > x) {
            dx = speedPlane;
            dy = 0;
            x += dx;
            y += dy;
        }

        if (e.getKeyCode() == KeyEvent.VK_LEFT && x > 0) {
            dx = -speedPlane;
            dy = 0;
            x += dx;
            y += dy;
        }

        if (e.getKeyCode() == KeyEvent.VK_UP && y > 0) {
            dx = 0;
            dy = -speedPlane;
            x += dx;
            y += dy;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN && heightPanel - yPlane > y) {
            dx = 0;
            dy = speedPlane;
            x += dx;
            y += dy;
        }
    }
}

class Obstacle {

    private static Image obstacle = new ImageIcon("Bird.jpg").getImage();
    private int dyObstacle = 3;
    protected static int heightObstacle = obstacle.getHeight(null);
    protected static int widthObstacle = obstacle.getWidth(null);
    private double random = Math.random();
    protected int yObstacle = 0 - heightObstacle;
    protected int xObstacle = (int) (random * (Plane.widthPanel - widthObstacle));

    public static Image getImageBird() {
        return obstacle;
    }

    public void moveObstacle() {
        yObstacle += dyObstacle;
    }

}
