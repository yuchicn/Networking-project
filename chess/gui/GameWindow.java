/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.gui;

import assets.ImageAssets;
import chess.Client;
import chess.game.Alignment;
import chess.game.ChessPieceType;
import chess.game.Coordinate;
import chess.gui.state.MainLobbyState;
import chess.gui.state.OfflineState;
import chess.gui.state.TitleState;
import java.awt.BorderLayout;
import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author yu-chi
 */
public class GameWindow extends javax.swing.JFrame implements IStateController, IGUIActionListener {

    private GUIState state;
    
    private String serverIP;
    private int serverPort;
    private chess.Client client = null;
    
    public GameWindow() throws IOException {
        this(TitleState.class);
    }
    
    public GameWindow(Class<? extends GUIState> initialState, String displayName) throws IOException {
        this(initialState);
        state.setProperty(StateProperties.DISPLAY_NAME, displayName);
    }
    
    /**
     * Creates new form GameWindow
     * @param initialState
     * @throws java.io.IOException
     */
    public GameWindow(Class<? extends GUIState> initialState) throws IOException {
        setLookAndFeel();
        loadProperties();
        initComponents();
        setIconImage(ImageAssets.loadChessPieceIcon(ChessPieceType.BLACK_ROOK));
        changeState(initializeState(initialState));
    }
    
    public void connect() {
        try {
            if (client != null) {
                client.connect(serverIP, serverPort, true);
            } else
                client = new Client(serverIP, serverPort, () -> this.state);
        } catch (Exception ex) {
            Logger.getLogger(GameWindow.class.getName()).log(Level.SEVERE, null, ex);
            changeState(new OfflineState(state, ex.getMessage()));
        }
    }
    
    private void loadProperties() throws IOException {
        serverIP = GameProperties.serverIP();
        serverPort = GameProperties.serverPort();
    }
    
    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private GUIState initializeState(Class<? extends GUIState> initialState) {
        try {
            Constructor<? extends GUIState> constructor =
                    initialState.getConstructor(IStateController.class);
            GUIState state = constructor.newInstance(this);
            return state;
        } catch (NoSuchMethodException | SecurityException | 
                InstantiationException | IllegalAccessException | 
                IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(GameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public final void interact(GUIAction action, Object... parameters) {
        System.out.println("Invoking " + action + " on " + state.getClass().getName() + " with " + Arrays.toString(parameters));
        state.interact(action, parameters);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chess");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (client != null)
            client.gracefulQuit();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(GameWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formWindowClosing

    @Override
    public final void setIconImage(Image image) {
        super.setIconImage(image); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public final IGUIActionListener guiListener() {
        return this;
    }

    @Override
    public final GUIState guiState() {
        return state;
    }
    
    public final void changeState(GUIState newState) {
        if (newState == null)
            throw new NullPointerException("null newState");
        if (newState.getClass().isInstance(this.state)) {
            Logger.getLogger(GameWindow.class.getName()).log(Level.WARNING, "Attempted to reload state {0}", this.state.getClass().getName());
            return;
        }
        if (this.state != null) {
            this.state.unloadState();
            this.getContentPane().remove(this.state);
        }
        this.state = newState;
        this.state.initializeState();
        this.getContentPane().add(this.state, BorderLayout.CENTER);
        this.pack();
        this.setMinimumSize(this.getSize());
        this.setResizable(this.state.isFlexible());
    }

    @Override
    public void gui_changeState(GUIState newState) {
        changeState(newState);
    }

    @Override
    public void gui_sendMessage(String message) {
        // TODO: client.sendChatMessage
        client.chat(message);
    }

    @Override
    public void gui_registerClient(String name) {
        client.register(name);
    }

    @Override
    public void gui_unregisterClient() {
        client.unregister();
    }

    @Override
    public void gui_hostSession() {
        client.makeGame();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void gui_switchColors(Alignment color) {
        client.pickColor(color.toString());
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void gui_toggleReady(boolean readyState) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        state.setProperty(StateProperties.MY_READY_STATE, readyState);
        client.ready(StateProperties.READY_STATE_CODE(readyState));
    }

    @Override
    public void gui_leaveSession() {
        client.leaveLobby();
        
        final GUIState sessionState = state;
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (guiState() == sessionState)
                    gui_changeState(new MainLobbyState(sessionState));
            }
        }, 3000);
    }

    @Override
    public void joinGame(String sessionName) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        client.joinGame(sessionName);
    }

    @Override
    public void gui_listPlayers() {
        client.list();
        client.askForUpdatedLeaderboard();
    }

    @Override
    public void gui_startGame() {
        client.startGame();
    }

    @Override
    public void gui_reconnect() {
        connect();
    }

    @Override
    public void gui_draw(chess.gui.AgreementState action) {
        switch (action) {
            case ACCEPT:
                client.agreeOnDraw(); break;
            case DECLINE: 
                client.declineDraw(); break;
            case REQUEST: 
                client.requestDraw(); break;
            case RETRACT: 
                client.cancelDrawRequest(); break;
            default:
                throw new IllegalStateException("state undefined: no draw action provided");
        }
    }

    @Override
    public void gui_forfeit() {
        client.forfeit();
    }

    @Override
    public void gui_move(Coordinate from, Coordinate to) {
        client.move(from, to);
    }

    @Override
    public void gui_ackMove(String player, Coordinate from, Coordinate to) {
        client.ackMove(player, from, to);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}