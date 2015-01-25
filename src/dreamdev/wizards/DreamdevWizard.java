package dreamdev.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import java.security.ProtectionDomain;
import java.security.CodeSource;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;


public class DreamdevWizard extends Wizard implements INewWizard 
{
	private DreamdevWizardPage page;
	private ISelection selection;


	public DreamdevWizard() 
	{
		super();
		setNeedsProgressMonitor(true);
	}

	public void addPages() 
	{
		page = new DreamdevWizardPage(selection);
		addPage(page);
	}

	public boolean performFinish() 
	{
		final String projectName = page.getProjectName();
		final String projectLocation = page.getProjectLocation();
		final String gccLocation = page.getGccLocation();
		final String kosLocation = page.getKosLocation();
		final String kosPortsLocation = page.getKosPortsLocation();
		IRunnableWithProgress op = new IRunnableWithProgress() 
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException 
			{
				try 
				{
					doFinish(projectName, projectLocation, gccLocation, kosLocation, kosPortsLocation, monitor);
				} catch (CoreException e) 
				{
					throw new InvocationTargetException(e);
				} finally 
				{
					monitor.done();
				}
			}
		};
		
		try 
		{
			getContainer().run(true, false, op);
		} 
		catch (InterruptedException e) 
		{
			return false;
		} 
		catch (InvocationTargetException e) 
		{
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	private void doFinish(String projectName, 
						  String projectLocation,
						  String gccLocation,
						  String kosLocation,
						  String kosPortsLocation, 
						  IProgressMonitor monitor) throws CoreException 
	{
		monitor.beginTask("Creating " + projectName, 2);
		
		String projectPath = projectLocation + "/" + projectName;
			
		try 
		{	
			createDir(projectPath + "/Release");
			
			createDir(projectPath);
			copyInternalFile("templates/project", 	projectPath + "/.project");
			copyInternalFile("templates/cproject", 	projectPath + "/.cproject");
			
			createDir(projectPath + "/.settings");
			copyInternalFile("templates/settings/language.settings.xml", 						projectPath + "/.settings/language.settings.xml");
			copyInternalFile("templates/settings/org.eclipse.cdt.codan.core.prefs", 			projectPath + "/.settings/org.eclipse.cdt.codan.core.prefs");
			copyInternalFile("templates/settings/org.eclipse.cdt.core.prefs", 					projectPath + "/.settings/org.eclipse.cdt.core.prefs");
			copyInternalFile("templates/settings/org.eclipse.cdt.managedbuilder.core.prefs", 	projectPath + "/.settings/org.eclipse.cdt.managedbuilder.core.prefs");
			copyInternalFile("templates/settings/org.eclipse.ltk.core.refactoring.prefs", 		projectPath + "/.settings/org.eclipse.ltk.core.refactoring.prefs");
			
			createDir(projectPath + "/Debug");
			copyInternalFile("templates/Debug/romdisk", 		projectPath + "/Debug/romdisk");
			copyInternalFile("templates/Debug/bin2o.sh", 		projectPath + "/Debug/bin2o.sh");
			copyInternalFile("templates/Debug/makefile", 		projectPath + "/Debug/makefile");
			copyInternalFile("templates/Debug/objects.mk", 		projectPath + "/Debug/objects.mk");
			copyInternalFile("templates/Debug/sources.mk", 		projectPath + "/Debug/sources.mk");
			copyInternalFile("templates/Debug/subdir.mk", 		projectPath + "/Debug/subdir.mk");
			
			createDir(projectPath + "/Release");
			copyInternalFile("templates/Release/romdisk", 		projectPath + "/Release/romdisk");
			copyInternalFile("templates/Release/bin2o.sh", 		projectPath + "/Release/bin2o.sh");
			copyInternalFile("templates/Release/makefile", 		projectPath + "/Release/makefile");
			copyInternalFile("templates/Release/objects.mk",	projectPath + "/Release/objects.mk");
			copyInternalFile("templates/Release/sources.mk", 	projectPath + "/Release/sources.mk");
			copyInternalFile("templates/Release/subdir.mk", 	projectPath + "/Release/subdir.mk");
			
			String settingsContent = readTextFile(projectPath + "/.settings/org.eclipse.cdt.core.prefs", StandardCharsets.UTF_8);
			settingsContent = settingsContent.replace("%%GCC_BASE%%", gccLocation);
			settingsContent = settingsContent.replace("%%KOS_BASE%%", kosLocation);
			settingsContent = settingsContent.replace("%%KOS_PORTS_BASE%%", kosPortsLocation);
			settingsContent = settingsContent.replace("%%KOS_ROMDISK_DIR%%", "");
			
			BufferedWriter writer = new BufferedWriter( new FileWriter( projectPath + "/.settings/org.eclipse.cdt.core.prefs"));
		    writer.write(settingsContent);
			writer.close();
			
			String cprojectContent = readTextFile(projectPath + "/.cproject", StandardCharsets.UTF_8);
			cprojectContent = cprojectContent.replace("%%PROJECT_NAME%%", projectName);
			
			writer = new BufferedWriter( new FileWriter( projectPath + "/.cproject"));
		    writer.write(cprojectContent);
			writer.close();
			
			String projectContent = readTextFile(projectPath + "/.project", StandardCharsets.UTF_8);
			projectContent = projectContent.replace("%%PROJECT_NAME%%", projectName);
			
			writer = new BufferedWriter( new FileWriter( projectPath + "/.project"));
		    writer.write(projectContent);
			writer.close();
		} 
		catch (IOException e) 
		{
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() 
		{
			public void run() 
			{
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				/*try 
				{
					IDE.openEditor(page, "", true);
				}
				catch (PartInitException e) 
				{
				}*/
			}
		});
		monitor.worked(1);
	}
	
	private String readTextFile(String path, Charset encoding) throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	private void createDir(String dirPath)
	{
		File dir = new File(dirPath);
		if (!dir.isDirectory())
		{
			dir.mkdir();
		}
	}
	
	private void copyInternalFile(String src, String dst) throws IOException
	{
		InputStream istream = openInternalStream(src);
		OutputStream ostream = new FileOutputStream(dst);
		
		int c;
		while ((c = istream.read()) != -1) 
		{
			ostream.write(c);
        }
		istream.close();
		ostream.close();
	}

	private InputStream openInternalStream(String filePath) 
	{
		ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
		CodeSource codeSource = protectionDomain.getCodeSource();
		URL location = codeSource.getLocation();
		File file = new File(location.getFile());
		
		InputStream resourceStream = null;
		try 
		{
			if (file.isFile()) 
			{
			    JarFile jar = new JarFile(file);
			    JarEntry xslEntry = jar.getJarEntry(filePath);
			    resourceStream = jar.getInputStream(xslEntry);
			} 
			else 
			{
			    resourceStream = new FileInputStream(file.getAbsolutePath() + "/" + filePath);
			}
		}
		catch (IOException e) 
		{
		}
		
		return resourceStream;
	}

	private void throwCoreException(String message) throws CoreException 
	{
		IStatus status = new Status(IStatus.ERROR, "dreamdev", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) 
	{
		this.selection = selection;
	}
}