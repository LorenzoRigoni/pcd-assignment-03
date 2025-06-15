package ass03.view;

import ass03.utils.SimulationParams;
import ass03.model.Boid;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.util.Hashtable;

public class BoidsView implements ChangeListener {

	private final JFrame frame;
	private BoidsPanel boidsPanel;
	private final JSlider cohesionSlider, separationSlider, alignmentSlider;
	private final JButton suspendResumeButton;
	private final GuiActorWrapper guiActorWrapper;
	private final int width, height;
	private boolean isStopped;
	private boolean isSuspended;
	
	public BoidsView(int width, int height, GuiActorWrapper guiActorWrapper) {
		this.width = width;
		this.height = height;
		this.guiActorWrapper = guiActorWrapper;
		this.isStopped = true;
		this.isSuspended = false;
		
		frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel cp = new JPanel();
		LayoutManager layout = new BorderLayout();
		cp.setLayout(layout);

        boidsPanel = new BoidsPanel(this);
		cp.add(BorderLayout.CENTER, boidsPanel);

		JPanel initialPanel = this.initialAndStopPanel(cp);

		this.suspendResumeButton = new JButton("Suspend");
		suspendResumeButton.setEnabled(false);
		suspendResumeButton.addActionListener(e -> {
			if (this.isSuspended) {
				this.isSuspended = false;
				this.suspendResumeButton.setText("Suspend");
				this.guiActorWrapper.sendCommandToActor(SimulationCommands.RESUME_SIMULATION);
			} else {
				this.isSuspended = true;
				this.suspendResumeButton.setText("Resume");
				this.guiActorWrapper.sendCommandToActor(SimulationCommands.SUSPEND_SIMULATION);
			}
		});

        JPanel slidersPanel = new JPanel();
        
        cohesionSlider = makeSlider();
        separationSlider = makeSlider();
        alignmentSlider = makeSlider();

		slidersPanel.add(this.suspendResumeButton);
        slidersPanel.add(new JLabel("Separation"));
        slidersPanel.add(separationSlider);
        slidersPanel.add(new JLabel("Alignment"));
        slidersPanel.add(alignmentSlider);
        slidersPanel.add(new JLabel("Cohesion"));
        slidersPanel.add(cohesionSlider);

		cp.add(BorderLayout.NORTH, initialPanel);
		cp.add(BorderLayout.SOUTH, slidersPanel);

		frame.setContentPane(cp);	
		
        frame.setVisible(true);
	}

	private JPanel initialAndStopPanel(JPanel cp) {
		final JPanel statePanel = new JPanel();
		statePanel.setLayout(new FlowLayout());

		final JLabel numOfBoidsLabel = new JLabel("Enter the number of boids (it must be a positive integer):");
		final JTextField numOfBoidsField = new JTextField(15);

		final JButton startStopButton = new JButton("Start");
		startStopButton.addActionListener(e -> {
			if(this.isInputPositiveInteger(numOfBoidsField.getText()) && this.isStopped) {
				this.suspendResumeButton.setEnabled(true);
				this.isStopped = false;
				int numOfBoids = Integer.parseInt(numOfBoidsField.getText());
				cp.remove(this.boidsPanel);
				this.boidsPanel = new BoidsPanel(this);
				cp.add(BorderLayout.CENTER, boidsPanel);
				cp.revalidate();
				cp.repaint();
				this.guiActorWrapper.sendCommandToActor(SimulationCommands.startSimulation(numOfBoids));
			} else if(!this.isStopped && !this.isSuspended) {
				this.isStopped = true;
				this.suspendResumeButton.setEnabled(false);
				this.guiActorWrapper.sendCommandToActor(SimulationCommands.STOP_SIMULATION);
			} else if(this.isSuspended) {
				JOptionPane.showMessageDialog(frame, "You cannot stop the simulation while the simulation is suspended");
			} else {
				JOptionPane.showMessageDialog(frame, "The input entered is not a positive integer");
			}

			if (this.isStopped) {
				startStopButton.setText("Start");
				numOfBoidsLabel.setVisible(true);
				numOfBoidsField.setVisible(true);
			} else {
				startStopButton.setText("Stop");
				numOfBoidsLabel.setVisible(false);
				numOfBoidsField.setVisible(false);
			}
		});

		statePanel.add(BorderLayout.WEST, numOfBoidsLabel);
		statePanel.add(BorderLayout.WEST, numOfBoidsField);
		statePanel.add(BorderLayout.CENTER, startStopButton);
		return statePanel;
	}

	private JSlider makeSlider() {
		var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);        
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		Hashtable labelTable = new Hashtable<>();
		labelTable.put( 0, new JLabel("0") );
		labelTable.put( 10, new JLabel("1") );
		labelTable.put( 20, new JLabel("2") );
		slider.setLabelTable( labelTable );
		slider.setPaintLabels(true);
        slider.addChangeListener(this);
		return slider;
	}
	
	public void update(int frameRate, java.util.List<Boid> boids) {
		SwingUtilities.invokeLater(() -> {
			boidsPanel.setFrameRate(frameRate, boids);
			boidsPanel.repaint();
		});
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == separationSlider) {
			var val = separationSlider.getValue();
			this.guiActorWrapper.sendCommandToActor(SimulationCommands.setParams(SimulationParams.SEPARATION, 0.1 * val));
		} else if (e.getSource() == cohesionSlider) {
			var val = cohesionSlider.getValue();
			this.guiActorWrapper.sendCommandToActor(SimulationCommands.setParams(SimulationParams.COHESION, 0.1 * val));
		} else if (e.getSource() == alignmentSlider) {
			var val = alignmentSlider.getValue();
			this.guiActorWrapper.sendCommandToActor(SimulationCommands.setParams(SimulationParams.ALIGNMENT, 0.1 * val));
		}
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private boolean isInputPositiveInteger(String input) {
		try  {
			return Integer.parseInt(input) > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
