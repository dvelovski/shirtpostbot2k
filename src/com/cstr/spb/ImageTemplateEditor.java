/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import static com.cstr.spb.EditRectangle.CORNER_BL;
import static com.cstr.spb.EditRectangle.CORNER_BR;
import static com.cstr.spb.EditRectangle.CORNER_TL;
import static com.cstr.spb.EditRectangle.CORNER_TR;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author Dimitri Velovski
 */
public class ImageTemplateEditor extends JPanel implements MouseMotionListener, MouseListener{
    
    public ImageTemplate iTemplate;
    public int grabRectangleSize = 6;
    public int moveHandleSize = 16;
    ArrayList<EditRectangle> rectangles = new ArrayList<>();
    EditRectangle selectedRectangle = null;
    EditRectangle dragTarget = null;
    BasicStroke outlineStroke = new BasicStroke(2);
    EditorWindowForm myOwner = null;
    
    BufferedImage moveToolIcon;
    BufferedImage imageTypeIcon;
    BufferedImage textTypeIcon;
    BufferedImage boxTypeIcon;
    
    ShirtTemplateObject sTO = null;
    
    public boolean shirtEditor = false; //if we are not a shirt editor, we will be a template editor
    public EditRectangle shirtRectangle = new EditRectangle();
    
    public int editRegionWidth = 525;
    public int editRegionHeight = 700;
    
    public Color editorBackground = new Color(97,97,97);
    public Color outlineTemplate = new Color(117,117,117);
    public Color selectionColour = new Color(179,229,252);
    public Color imageColour = new Color(77,208,225);
    public Color textObjectColour = new Color(77,182,172);
    public Color snapLineColour = new Color(244,143,177);
    
    public int snapXThreshold = 3;
    public int snapYThreshold = 3;
    public ArrayList<SnapLines> snapOpportunities = new ArrayList<>();
    
    public void setTemplate(ImageTemplate i){
        commitChanges();
        iTemplate = i;
        
        updateRectangles();
    }
    public void setShirtTemplate(ShirtTemplateObject s){
        if (shirtEditor){
            if (sTO != null){
                commitChanges();
            }
        }
        
        sTO = s;
        if (s.cached == null){
            s.loadImage();
        }
        myOwner.setSelectedShirt(sTO);
        
        updateRectangles();
    }
    public void commitChanges(){
         //commit the changes
        if (shirtEditor){
            if (sTO != null){
                sTO.locationX = (float) shirtRectangle.position.x / (float) this.getWidth();
                sTO.locationY = (float) shirtRectangle.position.y / (float) this.getHeight();
                sTO.width = (float) shirtRectangle.position.width / (float) this.getWidth();
                sTO.height = (float) shirtRectangle.position.height / (float) this.getHeight();   
            }
        }else{            
            if (iTemplate != null){            
                for (int x = 0; x < iTemplate.objects.size(); x++){
                    ImageTemplateObject ob = iTemplate.objects.get(x);
                    Rectangle corr = rectangles.get(x).position;
                    
                    int startX = getWidth() / 2 - (editRegionWidth / 2);
                    int startY = getHeight() / 2 - (editRegionHeight / 2);
                    
                    ob.locationX = (float) (corr.x - startX) / (float) this.getWidth();
                    ob.locationY = (float) (corr.y - startY) / (float) this.getHeight();
                    ob.width = (float) corr.width / (float) editRegionWidth;
                    ob.height = (float) corr.height / (float) editRegionHeight;         
                }
            }
        }
    }
    public void updateRectangles(){
        rectangles.clear();
        
        if (shirtEditor){
            shirtRectangle.position = new Rectangle((int) (sTO.locationX * this.getWidth()), (int) (sTO.locationY * this.getHeight()), 
                    (int) (sTO.width * this.getWidth()), (int) (sTO.height * this.getHeight()));
            rectangles.add(shirtRectangle);
        }else{            
            for (ImageTemplateObject iO : iTemplate.objects){
                EditRectangle er = new EditRectangle();
                er.link = iO;
                int startX = getWidth() / 2 - (editRegionWidth / 2);
                int startY = getHeight() / 2 - (editRegionHeight / 2);
                    
                er.position = new Rectangle((int) (iO.locationX * this.getWidth()) + startX, (int) (iO.locationY * this.getHeight()) + startY, 
                        (int) (iO.width * editRegionWidth), (int) (iO.height * editRegionHeight));
                rectangles.add(er);
            }
        }
    }
    
    public ImageTemplateEditor() {
        addMouseListener(this);
        addMouseMotionListener(this);
        
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ImageTemplateEditor.this.repaint();
            }
        }, 0, 33);
        
        try {
            moveToolIcon = ImageIO.read(new File("resources/interface/move.png"));
            imageTypeIcon = ImageIO.read(new File("resources/interface/pic.png"));
            textTypeIcon = ImageIO.read(new File("resources/interface/text.png"));
            boxTypeIcon = ImageIO.read(new File("resources/interface/box.png"));
        } catch (IOException ex) {
            Logger.getLogger(ImageTemplateEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(editorBackground);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        
                         
        if (sTO != null && sTO.cached != null && shirtEditor){
            g2.drawImage(sTO.cached, 0, 0, this.getWidth(), this.getHeight(), null);
            if (shirtEditor){
                renderRectangleGizmos(g2, shirtRectangle);            
            }
        }
        
        if (iTemplate != null){   
            g2.setColor(Color.black);
            
            g2.setStroke(outlineStroke);
            //we now draw a client region
            g2.setColor(Color.white);
            g2.fillRect(this.getWidth() / 2 - (editRegionWidth / 2), this.getHeight() / 2 - (editRegionHeight / 2), editRegionWidth, editRegionHeight);
            g2.setColor(outlineTemplate);
            g2.drawRect(this.getWidth() / 2 - (editRegionWidth / 2), this.getHeight() / 2 - (editRegionHeight / 2), editRegionWidth, editRegionHeight);
            
            int textIndex = 0;

            for (EditRectangle er : rectangles){
                if (er == selectedRectangle){
                    g2.setColor(selectionColour);
                }else{
                    g2.setColor(er.link.templateObjectType == ImageTemplateObject.TYPE_IMAGE ? imageColour : textObjectColour);
                }
                             
                if (!shirtEditor && er != shirtRectangle){
                    renderRectangleGizmos(g2, er);
                }
                
                if (er.link.templateObjectType == ImageTemplateObject.TYPE_TEXT){
                    textIndex++;
                    g2.setColor(Color.black);
                    g2.fillRect(er.position.x +16, er.position.y + er.position.height - grabRectangleSize * 3, 40, 20);
                    g2.setColor(Color.white);
                    g2.drawString("Line #" + textIndex, er.position.x + 18, er.position.y + er.position.height - grabRectangleSize);
                }
                BufferedImage toDraw = imageTypeIcon;
                switch (er.link.templateObjectType){
                    case ImageTemplateObject.TYPE_TEXT:
                        toDraw = textTypeIcon;
                        break;
                    case ImageTemplateObject.TYPE_IMAGE:
                        toDraw = imageTypeIcon;
                        break;
                    case ImageTemplateObject.TYPE_BOX:
                        toDraw = boxTypeIcon;
                        break;
                }
                g2.drawImage(toDraw, er.position.x, er.position.y + er.position.height - moveHandleSize - grabRectangleSize, 16, 16, null);
                
            }
            for (SnapLines sL : snapOpportunities){
                g2.setColor(snapLineColour);
                g2.setStroke(new BasicStroke(2));
                if (sL.snapX){
                    g2.drawLine((int) sL.locX, 0, (int) sL.locX, getHeight());
                }
                if (sL.snapY){
                    g2.drawLine(0, (int) sL.locY, getWidth(), (int) sL.locY);
                }
            }
        }
    }
    
    public void renderRectangleGizmos(Graphics2D g2, EditRectangle er){
        Stroke s= g2.getStroke();
        g2.setStroke(outlineStroke);
        g2.draw(er.position);
        g2.setStroke(s);

        g2.setColor(Color.white);
        g2.fillRect(er.position.x, er.position.y, grabRectangleSize, grabRectangleSize);
        g2.fillRect(er.position.x + er.position.width - grabRectangleSize, er.position.y, grabRectangleSize, grabRectangleSize);
        g2.fillRect(er.position.x, er.position.y + er.position.height - grabRectangleSize, grabRectangleSize, grabRectangleSize);
        g2.fillRect(er.position.x + er.position.width - grabRectangleSize, er.position.y + er.position.height - grabRectangleSize, grabRectangleSize, grabRectangleSize);
        g2.drawImage(moveToolIcon, er.position.x + (er.position.width / 2) - (moveHandleSize / 2), er.position.y + (er.position.height / 2) - (moveHandleSize / 2), moveHandleSize, moveHandleSize, null);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Rectangle hitTest = new Rectangle(e.getX(), e.getY(), 1, 1);
        if (dragTarget == null){
            for (EditRectangle er : rectangles){
                if (er.position.intersects(hitTest)){
                    if (er.cornerSelectIndex == -1){
                        //act on it

                        //now we check intersection with the handles
                        int relX = e.getX() - er.position.x;
                        int relY = e.getY() - er.position.y;

                        boolean use = false;
                        
                        //top left
                        if (relX <= grabRectangleSize && relY <= grabRectangleSize){
                            er.cornerSelectIndex = 1;
                            use = true;
                        //top right
                        }else if (relX >= er.position.width - grabRectangleSize && relY <= grabRectangleSize){
                            er.cornerSelectIndex = 2;                        
                            use = true;
                        //bottom right
                        }else if (relX >= er.position.width - grabRectangleSize && relY >= er.position.height - grabRectangleSize){
                            er.cornerSelectIndex = 3;                        
                            use = true;
                        //bottom left
                        }else if (relX <= grabRectangleSize && relY >= er.position.height - grabRectangleSize){
                            er.cornerSelectIndex = 4;                        
                            use = true;
                        }else if ((relX >= (er.position.width / 2) - (moveHandleSize / 2)) && (relX <= (er.position.width / 2) + (moveHandleSize / 2)) && (relY >= (er.position.height / 2) - (moveHandleSize / 2)) && (relY <= (er.position.height / 2) + (moveHandleSize / 2))){
                            er.cornerSelectIndex = 5;
                            use = true;
                            er.clickRelX = relX;
                            er.clickRelY = relY;
                        }
                        
                        if (use){
                            selectedRectangle = dragTarget = er;
                            setSelectedRectangle(er);
                            break;
                        }
                    }
                    
                }
            }
        }          
        if (dragTarget != null){      
            ResizeDelta rd = new ResizeDelta();
            Rectangle pos = new Rectangle(dragTarget.position.x, dragTarget.position.y, dragTarget.position.width, dragTarget.position.height);
            
            switch (dragTarget.cornerSelectIndex){
                case 1:
                    dragTarget.position.width -= e.getX() - dragTarget.position.x;
                    dragTarget.position.height -= e.getY() - dragTarget.position.y;   
                    dragTarget.position.x = e.getX();
                    dragTarget.position.y = e.getY();
                    break;
                case 2:
                    dragTarget.position.width = e.getX() - dragTarget.position.x;
                    dragTarget.position.height -= e.getY() - dragTarget.position.y;                    
                    dragTarget.position.y = e.getY();
                    break;
                case 3:
                    dragTarget.position.width = e.getX() - dragTarget.position.x;
                    dragTarget.position.height = e.getY() - dragTarget.position.y;    
                    break;
                case 4:
                    dragTarget.position.width -= e.getX() - dragTarget.position.x;
                    dragTarget.position.height = e.getY() - dragTarget.position.y;    
                    dragTarget.position.x = e.getX();
                    break;
                case 5:
                    dragTarget.position.x = e.getX() - dragTarget.clickRelX;
                    dragTarget.position.y = e.getY() - dragTarget.clickRelY;
                    break;
            }
            //getSnapOpportunities(dragTarget);
            processSnapping(dragTarget, pos, rd);
            //dragTarget.position.translate(rd.deltaX, rd.deltaY);
            if (rd.deltaX != 0){ //we snapped x axis     
                if (dragTarget.cornerSelectIndex != 2 && dragTarget.cornerSelectIndex != 3){
                    dragTarget.position.width += rd.deltaX;
                    dragTarget.position.x -= rd.deltaX;
                }else{                    
                    dragTarget.position.width -= rd.deltaX;                    
                }
            }
            if (rd.deltaY != 0){ //we snapped y axis
                if (dragTarget.cornerSelectIndex != 3 && dragTarget.cornerSelectIndex != 4){                    
                    dragTarget.position.height += rd.deltaY;
                    dragTarget.position.y -= rd.deltaY;
                }else{
                    dragTarget.position.height -= rd.deltaY;
                }
            }
        }
        
    }
    
    public void processSnapping(EditRectangle dTarget, Rectangle initial, ResizeDelta rDelta){
        snapOpportunities.clear();
        
        boolean xAxisSnapped = false;
        boolean yAxisSnapped = false;
                
        for (EditRectangle er : rectangles){
            if (er == dTarget){
                continue;
            }
            Point myTL = dTarget.getCorner(CORNER_TL);
            Point myTR = dTarget.getCorner(CORNER_TR);
            Point myBR = dTarget.getCorner(CORNER_BR);
            Point myBL = dTarget.getCorner(CORNER_BL);  
            
            Point testTL = er.getCorner(CORNER_TL);
            Point testTR = er.getCorner(CORNER_TR);
            Point testBR = er.getCorner(CORNER_BR);
            Point testBL = er.getCorner(CORNER_BL);  
            
            boolean x1 = false , x2 = false , y1 = false, y2 = false;
            
            
            if (dTarget.cornerSelectIndex == CORNER_TL){                
                if (!yAxisSnapped){
                    y1 = testAndAddOpportunities(dTarget, er, myTL, testTR, 0, 0, true, snapYThreshold, rDelta);
                    y2 = testAndAddOpportunities(dTarget, er, myTL, testBR, 0, 0, true, snapYThreshold, rDelta);
                }
                if (!xAxisSnapped){
                    x1 = testAndAddOpportunities(dTarget, er, myTL, testBR, 0, 0, false, snapXThreshold, rDelta);
                    x2 = testAndAddOpportunities(dTarget, er, myTL, testBL, 0, 0, false, snapXThreshold, rDelta);
                }
            }
            if (dTarget.cornerSelectIndex == CORNER_TR){    
                if (!yAxisSnapped){
                    y1 = testAndAddOpportunities(dTarget, er, myTR, testTL, 0, 0, true, snapYThreshold, rDelta);
                    y2 = testAndAddOpportunities(dTarget, er, myTR, testBR, 0, 0, true, snapYThreshold, rDelta);
                }
                if (!xAxisSnapped){
                    x1 = testAndAddOpportunities(dTarget, er, myTR, testBR, 0, 0, false, snapXThreshold, rDelta);
                    x2 = testAndAddOpportunities(dTarget, er, myTR, testBL, 0, 0, false, snapXThreshold, rDelta);
                }            
            }
            if (dTarget.cornerSelectIndex == CORNER_BR){    
                if (!yAxisSnapped){
                    y1 = testAndAddOpportunities(dTarget, er, myBR, testTL, 0, 0, true, snapYThreshold, rDelta);
                    y2 = testAndAddOpportunities(dTarget, er, myBR, testBR, 0, 0, true, snapYThreshold, rDelta);
                }
                if (!xAxisSnapped){
                    x1 = testAndAddOpportunities(dTarget, er, myBR, testBL, 0, 0, false, snapXThreshold, rDelta);
                    x2 = testAndAddOpportunities(dTarget, er, myBR, testBR, 0, 0, false, snapXThreshold, rDelta);
                }            
            }
            if (dTarget.cornerSelectIndex == CORNER_BL){    
                if (!yAxisSnapped){
                    y1 = testAndAddOpportunities(dTarget, er, myBL, testTR, 0, 0, true, snapYThreshold, rDelta);
                    y2 = testAndAddOpportunities(dTarget, er, myBL, testBR, 0, 0, true, snapYThreshold, rDelta);
                }
                if (!xAxisSnapped){
                    x1 = testAndAddOpportunities(dTarget, er, myBL, testTL, 0, 0, false, snapXThreshold, rDelta);
                    x2 = testAndAddOpportunities(dTarget, er, myBL, testBR, 0, 0, false, snapXThreshold, rDelta);
                }            
            }
                
            if (x1 || x2){
                xAxisSnapped = true;
            }
            if (y1 || y2){
                yAxisSnapped = true;
            }
        }
        System.out.println(rDelta);
    }
    
    boolean testAndAddOpportunities(EditRectangle dTarget, EditRectangle er, Point a, Point b, int snapXOffset, int snapYOffset, boolean axis, int threshold, ResizeDelta rD){
        boolean test = dTarget.testPoint(a, b, axis, snapYThreshold);
        if (test){
            snapOpportunities.add(new SnapLines().setPosition(b.x + snapXOffset, b.y + snapYOffset).setSnaps((axis ? false : true), (axis ? true : false)));
            if (axis){
                rD.deltaY = a.y - b.y;                    
            }else{
                rD.deltaX = a.x - b.x;
            }
        }
        return test;
    }

    public int[] getSnapOpportunities(EditRectangle dTarget){
        snapOpportunities.clear();
        int[] result = {-1, -1, -1, -1}; //x , y, width, height
        
        if (dTarget.cornerSelectIndex == 1 || dTarget.cornerSelectIndex == 4){
            for (EditRectangle er : rectangles){
                if (er == dTarget){
                    continue;
                }
                if (dTarget.position.x >= er.position.x - snapXThreshold && dTarget.position.x <= er.position.x + snapXThreshold){
                    snapOpportunities.add(new SnapLines().
                            setPosition(er.position.x, er.position.y).
                            setSnaps(true, false));
                    result[2] = er.position.x + dTarget.position.width;
                    dTarget.position.setLocation(er.position.x, dTarget.position.y);
                    break;
                }
                if (dTarget.cornerSelectIndex == 1){
                    doTopYTest(dTarget, er);
                }
                if (dTarget.cornerSelectIndex == 4){
                    doBottomYTest(dTarget, er);
                }
            }
        }else if (dTarget.cornerSelectIndex == 2 || dTarget.cornerSelectIndex == 3){
            for (EditRectangle er : rectangles){
                if (er == dTarget){
                    continue;
                }
                if (dTarget.position.x + dTarget.position.width >= er.position.x + er.position.width - snapXThreshold &&
                        dTarget.position.x + dTarget.position.width <= er.position.x + er.position.width + snapXThreshold){
                    snapOpportunities.add(new SnapLines().
                            setPosition(er.position.x + er.position.width, er.position.y).
                            setSnaps(true, false));
                    dTarget.position.setSize(er.position.x + er.position.width - dTarget.position.x, dTarget.position.height);
                    //dTarget.position.setLocation(er.position.x, dTarget.position.y);                    
                    break;
                }else if (dTarget.position.x + dTarget.position.width >= er.position.x - snapXThreshold &&
                        dTarget.position.x + dTarget.position.width <= er.position.x + snapXThreshold){
                    snapOpportunities.add(new SnapLines().
                            setPosition(er.position.x, er.position.y).
                            setSnaps(true, false));
                    dTarget.position.setSize(er.position.x - dTarget.position.x, dTarget.position.height);
                }
                
                if (dTarget.cornerSelectIndex == 2){
                    doTopYTest(dTarget, er);
                }
                if (dTarget.cornerSelectIndex == 3){
                    doBottomYTest(dTarget, er);
                }
                
            }
        }
        
        return result;
    }
    public void doTopYTest(EditRectangle dTarget, EditRectangle er){
        if (dTarget.position.y >= er.position.y - snapYThreshold && dTarget.position.y <= er.position.y + snapYThreshold){
            snapOpportunities.add(new SnapLines().
                    setPosition(er.position.x, er.position.y).
                    setSnaps(false, true));
            dTarget.position.setLocation(dTarget.position.x, er.position.y);            
        }
    }
    public void doBottomYTest(EditRectangle dTarget, EditRectangle er){
        if (dTarget.position.y + dTarget.position.height >= er.position.y + er.position.height - snapYThreshold && 
                dTarget.position.y + dTarget.position.height <= er.position.y + er.position.height + snapYThreshold){
            snapOpportunities.add(new SnapLines().
                    setPosition(er.position.x, er.position.y + er.position.height).
                    setSnaps(false, true));
            dTarget.position.setSize(dTarget.position.width, er.position.y + er.position.height - dTarget.position.y);
        }
    }
    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //we'll check for a hit on one of our client rectangles
        Rectangle hitTest = new Rectangle(e.getX(), e.getY(), 1, 1);
        for (EditRectangle er : rectangles){
            if (er.position.intersects(hitTest)){
                selectedRectangle = er;
                setSelectedRectangle(er);
                break;
            }
        }
    }
    
    public void setSelectedRectangle(EditRectangle er){
        if (!shirtEditor){            
            myOwner.selectTemplateObject(er.link);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragTarget != null){
            dragTarget.cornerSelectIndex = -1;
            dragTarget = null;
            
            snapOpportunities.clear();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}

class EditRectangle{
    public static final int CORNER_TL = 1;
    public static final int CORNER_TR = 2;
    public static final int CORNER_BR = 3;
    public static final int CORNER_BL = 4;
    
    
    public ImageTemplateObject link;
    public Rectangle position = new Rectangle();
    
    public int cornerSelectIndex = -1;
    
    public int clickRelX;
    public int clickRelY;
    
    public Point getCorner(int cornerID){
        switch (cornerID){
            case CORNER_TL:
                return position.getLocation();
            case CORNER_TR:
                Point p = position.getLocation();
                p.translate(position.width, 0);  
                return p;
            case CORNER_BR:
                Point pt = position.getLocation();
                pt.translate(position.width, position.height);  
                return pt;
            case CORNER_BL:
                Point po = position.getLocation();
                po.translate(0, position.height);
                return po;
            default:
                return position.getLocation();
        }
    }    
    /**
     * 
     * @param a
     * @param b
     * @param axis if axis = false, x, else y
     * @return 
     */
    public boolean testPoint(Point a, Point b, boolean axis, int threshold){
        if (!axis){
            return a.x >= b.x - threshold && a.x <= b.x + threshold;
        }else{
            return a.y >= b.y - threshold && a.y <= b.y + threshold;
        }
    }
}
class SnapLines{
    float locX;
    float locY;
    
    boolean snapX = true;
    boolean snapY = true;
    
    ArrayList<EditRectangle> gapXParticipants = new ArrayList<>();
    ArrayList<EditRectangle> gapYParticipants = new ArrayList<>();
    
    public SnapLines setPosition(float x, float y){
        locX = x;
        locY = y;
        return this;
    }
    public SnapLines setSnapX(boolean b){
        snapX = b;
        return this;
    }
    
    public SnapLines setSnapY(boolean b){
        snapY = b;
        return this;
    }
    public SnapLines setSnaps(boolean x, boolean y){
        snapX = x;
        snapY = y;
        return this;
    }
}
class ResizeDelta{
    int deltaX = 0;
    int deltaY = 0;
    int deltaW = 0;
    int deltaH = 0;

    @Override
    public String toString() {
        return "Delta X: " + deltaX + ", Delta Y: " + deltaY + ", Delta W: " + deltaW + ", Delta H: " + deltaH;
    }
}