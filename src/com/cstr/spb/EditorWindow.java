/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cstr.spb;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Admin
 */
public class EditorWindow extends JFrame{
    JTabbedPane navPane = new JTabbedPane();
    JPanel mainPanel = new JPanel();
    JPanel textPanel = new JPanel();
    JPanel templatePanel = new JPanel();
    
    JPanel templateSidebar = new JPanel();
    JComboBox templateList = new JComboBox();
    
    ImageTemplateEditor iEditor = new ImageTemplateEditor();
    
    public EditorWindow() throws HeadlessException {        
              
        super();        
        
        SwingUtilities.updateComponentTreeUI(this);
        
        this.setSize(1024, 768);
        this.setTitle("ShirtPostBot Management Window");
        //this.setLayout(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //navPane.setSize(800, 600);
        add(navPane);
        
        templatePanel.setLayout(null);
        
        templateSidebar.setSize(300, 768);
        templateSidebar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        templateSidebar.setLayout(new BoxLayout(templateSidebar, BoxLayout.Y_AXIS));
        
        JLabel lblAvailable = new JLabel("Available image templates:");
        lblAvailable.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvailable.setLocation(5, 5);
        
        templateList.setPreferredSize(new Dimension(100, 20));
        
        templateSidebar.add(lblAvailable);
        templateSidebar.add(templateList);
        templatePanel.add(templateSidebar);
        
        iEditor.setLocation(300, 5);
        iEditor.setSize(724, 758);
        iEditor.setTemplate(Main.iGenerator.getRandomTemplate());
        templatePanel.add(iEditor);
        
        navPane.addTab("Main", mainPanel);
        navPane.addTab("Text Templates", textPanel);
        navPane.addTab("Image Templates", templatePanel);
    }
}
