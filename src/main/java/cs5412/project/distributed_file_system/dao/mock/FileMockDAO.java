package cs5412.project.distributed_file_system.dao.mock;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import cs5412.project.distributed_file_system.dao.FileDAO;
import cs5412.project.distributed_file_system.pojo.File;

@Named
public class FileMockDAO implements FileDAO {

	@Override
	public int createFile(File file) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public boolean updateFile(File file) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public File getFileByFid(int fid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<File> getFileByParentDir(File parentDir) {
		ArrayList<File> files = new ArrayList<File>();
		File f = new File("file1", false);
		f.setFid(0);
		files.add(f);
		f = new File("file2", false);
		f.setFid(1);
		files.add(f);
		f = new File("dir1", true);
		f.setFid(2);
		files.add(f);
		return files;
	}

	@Override
	public boolean deleteFile(File file) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public File getRootDirForUser(int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean createHistory(int uid, int fidold, int fidnew, int type) {
		// TODO Auto-generated method stub
		return false;
	}

}
