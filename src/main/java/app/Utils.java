package app;

import java.io.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.treewalk.filter.*;

public class Utils {
	List<ObjectId> ids = new LinkedList<ObjectId>();
	List<Ref> call;
	List<fileInformation> informations = new LinkedList<fileInformation>();
	
	/*
	 * Consegui aceder ao ficheiro daqui para todos usarmos :)
	 * 
	 */
	
	//Classe nova para ser mais facil de passar informa��o sobre os ficheiros para fora
	public class fileInformation {
		Date timestamp;
		String fileName, tagName, tagDescription;		
		public fileInformation(Date timestamp, String filename, String tagName, String tagDescription) {
			this.timestamp = timestamp;
			this.fileName = filename;
			this.tagName = tagName;
			this.tagDescription = tagDescription;
		}
		public Date getTimestamp() {
			return timestamp;
		}
		public String getFileName() {
			return fileName;
		}
		public String getTagName() {
			return tagName;
		}
		public String getTagDescription() {
			return tagDescription;
		}
	}

	private static Git getGit() {
		File rep = new File("/Repositorio");
		Git git = null;
		if (rep.exists()) { // Repository exists, opening, and 
			try {
				git = Git.open(new File("/Repositorio/.git"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else { // Repository doesn't exist, going to create it.
			try {
				git = Git.cloneRepository()
				  .setURI("https://github.com/vbasto-iscte/ESII1920.git")
				  .setDirectory(new File("/Repositorio"))
				  .call();
			} catch (InvalidRemoteException e) {
				System.err.println("Error - Invalid Remote " + e);
				e.printStackTrace();
			} catch (TransportException e) {
				System.err.println("Error - Transport " + e);
				e.printStackTrace();
			} catch (GitAPIException e) {
				System.err.println("Error  - GitAPI " + e);
				e.printStackTrace();
			}
		
		}
		return git;
	}
	
	public static Repository getGitRepository() {
		Git git = getGit();
		return git.getRepository();
	}

	void readFile() throws RevisionSyntaxException, NoHeadException, GitAPIException {
		Repository repository = getGitRepository();
	
		
		//Buscar referencias para commits com tags e criar uma lista com todas as tags
		call = getGit().tagList().call();
		for (Ref ref : call) {
		    ids.add(ref.getObjectId());
		}
		// a RevWalk allows to walk over commits based on some filtering that is defined
		try {
			//Usar este ID para obter o ficheiro master mas n�o � preciso
			ObjectId lastCommitId = repository.resolve(Constants.HEAD);
			RevWalk revWalk = new RevWalk(repository);
			//Percorrer a lista criada de tags, e procurar commits com o ID das tags
			for(int i = 0; i<ids.size(); i++) {
				//Encontrar o commit com id da tag i
				RevCommit commit = revWalk.parseCommit(ids.get(i));
				
				//Receber informa��o da parte 4
				PersonIdent author = commit.getAuthorIdent();
				Date timestamp = author.getWhen();
				String fileTag = call.get(i).getName();
				String tagDescription = commit.getShortMessage();
				//Fim da primeira parte
				
				RevTree tree = commit.getTree();
				// e depois faz o download do covid19spreading.rdf
			try {
				TreeWalk treeWalk = new TreeWalk(repository);
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				treeWalk.setFilter(PathFilter.create("covid19spreading.rdf"));
				if (!treeWalk.next()) {
					treeWalk.close();
					throw new IllegalStateException("Did not find expected file 'covid19spreading.rdf'");
				}

				ObjectId objectId = treeWalk.getObjectId(0);
				ObjectLoader loader = repository.open(objectId);
				// and then one can the loader to read the file
				//loader.copyTo(System.out);
				byte[] bytes = loader.getBytes();
				FileOutputStream fos = new FileOutputStream("covid19spreading"+i+".rdf");
				
				//continua��o de recolha de informa��o parte 4
				String fileName = "covid19spreading"+i+".rdf";
				informations.add(new fileInformation(timestamp, fileName, fileTag, tagDescription ));
				//Fim de recolha de informa��o
				
				//System.out.println("Vou escrever o " + fos.toString());
				fos.write(bytes);
				fos.close();
				treeWalk.close();
			} catch (IOException e) {
				e.printStackTrace();
			}}
			
			revWalk.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	public List<fileInformation> getInformations() {
		try {
			readFile();
		} catch (RevisionSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return informations;
	}

	public static void main(String[] args) {
		try {
			new Utils().readFile();
		} catch (RevisionSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
