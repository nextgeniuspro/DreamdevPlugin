package dreamdev.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import java.io.*;



public class DreamdevWizardPage extends WizardPage 
{
	private Text projectNameText;
	private Text projectLocationText;
	private Text gccLocationText;
	private Text kosLocationText;
	private Text kosPortsLocationText;

	private ISelection selection;


	
	public DreamdevWizardPage(ISelection selection) 
	{
		super("wizardPage");
		setTitle("Dreamcast project settings");
		setDescription("This wizard generates a Sega Dreamcast C/C++ development project");
		this.selection = selection;
	}

	public void createControl(Composite parent) 
	{		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		// Project name
		Label projectNameLabel = new Label(container, SWT.NULL);
		projectNameLabel.setText("&Project name:");

		projectNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData projectGd = new GridData(GridData.FILL_HORIZONTAL);
		projectNameText.setLayoutData(projectGd);
		projectNameText.setText("");
		projectNameText.addModifyListener(new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				dialogChanged();
			}
		});
		skipCell(container);
		
		// Project location
		Label projectLocationLabel = new Label(container, SWT.NULL);
		projectLocationLabel.setText("&Project Location:");
		
		projectLocationText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData projectLocationGd = new GridData(GridData.FILL_HORIZONTAL);
		projectLocationText.setLayoutData(projectLocationGd);
		projectLocationText.setText("");
		projectLocationText.addModifyListener(new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				dialogChanged();
			}
		});

		Button projectBrowseButton = new Button(container, SWT.PUSH);
		projectBrowseButton.setText("Browse...");
		projectBrowseButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				projectLocationText.setText(handleDirectoryBrowse());
			}
		});
		
		// Compiler settings
		Label gccLocationLabel = new Label(container, SWT.NULL);
		gccLocationLabel.setText("&GCC Location:");
		
		gccLocationText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gccGd = new GridData(GridData.FILL_HORIZONTAL);
		gccLocationText.setLayoutData(gccGd);
		gccLocationText.setText("/opt/toolchains/dc");
		gccLocationText.addModifyListener(new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				dialogChanged();
			}
		});

		Button gccBrowseButton = new Button(container, SWT.PUSH);
		gccBrowseButton.setText("Browse...");
		gccBrowseButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				gccLocationText.setText(handleDirectoryBrowse());
			}
		});
		
		// KOS settings
		Label kosLocationLabel = new Label(container, SWT.NULL);
		kosLocationLabel.setText("&KOS Location:");
		
		kosLocationText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData kosGd = new GridData(GridData.FILL_HORIZONTAL);
		kosLocationText.setLayoutData(kosGd);
		kosLocationText.setText("/opt/toolchains/dc/kos");
		kosLocationText.addModifyListener(new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				dialogChanged();
			}
		});

		Button kosBrowseButton = new Button(container, SWT.PUSH);
		kosBrowseButton.setText("Browse...");
		kosBrowseButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				kosLocationText.setText(handleDirectoryBrowse());
			}
		});
		
		// KOS ports settings
		Label kosPortsLocationLabel = new Label(container, SWT.NULL);
		kosPortsLocationLabel.setText("&KOS-Ports Location:");
		
		kosPortsLocationText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData kosPortsGd = new GridData(GridData.FILL_HORIZONTAL);
		kosPortsLocationText.setLayoutData(kosPortsGd);
		kosPortsLocationText.setText("/opt/toolchains/dc/kos-ports");
		kosPortsLocationText.addModifyListener(new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				dialogChanged();
			}
		});

		Button kosPortsBrowseButton = new Button(container, SWT.PUSH);
		kosPortsBrowseButton.setText("Browse...");
		kosPortsBrowseButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				kosPortsLocationText.setText(handleDirectoryBrowse());
			}
		});
		
		dialogChanged();
		setControl(container);
	}
	
	private void skipCell(Composite container)
	{
		new Label(container, SWT.NULL);
	}

	private String handleDirectoryBrowse() 
	{		
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
		return dialog.open();
	}

	private void dialogChanged() 
	{
		if (getProjectName().length() == 0) 
		{
			updateStatus("Project name must be specified");
			return;
		}
		
		// Check project location
		File projectLocation = new File(getProjectLocation());		
		if (getProjectLocation().length() == 0 ||
			!projectLocation.isDirectory()) 
		{
			updateStatus("Project location must exist");
			return;
		}
		if (!projectLocation.canWrite()) {
			updateStatus("Project directory must be writtable");
			return;
		}
		
		// Check GCC location
		File gccLocation = new File(getGccLocation());
		File shLocation = new File(getGccLocation() + "/sh-elf");
		File armLocation = new File(getGccLocation() + "/arm-eabi");
		if (getGccLocation().length() == 0 ||
			!gccLocation.isDirectory()) 
		{
			updateStatus("GCC location must exist");
			return;
		}
		
		if (!shLocation.isDirectory() || !armLocation.isDirectory()) 
		{
			updateStatus("Cannot detect toolchain in GCC location directory");
			return;
		}
		
		// Check KOS location
		File kosLocation = new File(getKosLocation());
		if (!kosLocation.isDirectory()) 
		{
			updateStatus("KOS location must exist");
			return;
		}
		
		// Check KOS location
		File kosPortsLocation = new File(getKosPortsLocation());
		if (!kosPortsLocation.isDirectory()) 
		{
			updateStatus("KOS-Ports location must exist");
			return;
		}

		updateStatus(null);
	}

	private void updateStatus(String message) 
	{
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getProjectName() 
	{
		return projectNameText.getText();
	}
	
	public String getProjectLocation() 
	{
		return projectLocationText.getText();
	}
	
	public String getGccLocation() 
	{
		return gccLocationText.getText();
	}

	public String getKosLocation() 
	{
		return kosLocationText.getText();
	}
	
	public String getKosPortsLocation() 
	{
		return kosPortsLocationText.getText();
	}
}