package cs5412.project.distributed_file_system.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import cs5412.project.distributed_file_system.dao.FileDAO;
import cs5412.project.distributed_file_system.pojo.File;
import cs5412.project.distributed_file_system.pojo.User;

@Named
public class FileJdbcDAO implements FileDAO {

	@Inject
	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class FileMapper implements RowMapper<File> {
		public File mapRow(ResultSet rs, int rowNum) throws SQLException {
			File file = new File();
			file.setFid(rs.getInt("fid"));
			file.setName(rs.getString("fname"));
			file.setLocation(rs.getString("location"));
			file.setParentDir(rs.getInt("directory"));
			file.setHash(rs.getString("hash"));
			file.setReferenceCount(rs.getInt("hiscount"));
			file.setUid(rs.getInt("uid"));
			file.setDir(rs.getBoolean("isDir"));
			file.setHidden(rs.getBoolean("isHidden"));
			return file;
		}
	}

	private static final class UserMapper implements RowMapper<User> {
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setUid(rs.getInt("uid"));
			user.setUsername(rs.getString("uname"));
			user.setPassword(rs.getString("password"));
			user.setEmail(rs.getString("email"));
			user.setRootfid(rs.getInt("rootfid"));
			return user;
		}
	}

	public boolean createHistory(int uid, int fidold, int fidnew, int type) {
		try {
			// create
			if (type == 1) {
				this.jdbcTemplate
						.update("insert into History (timestamp, uid, File_fid_new, type) values (?, ?, ?, ?)",
								new Object[] { new Date(), uid, fidnew, type });
				// remove
			} else if (type == 2) {
				this.jdbcTemplate
						.update("insert into History (timestamp, uid, File_fid_old, type) values (?, ?, ?, ?)",
								new Object[] { new Date(), uid, fidold, type });
				// update
			} else if (type == 3) {
				this.jdbcTemplate
						.update("insert into History (timestamp, uid, File_fid_old, File_fid_new, type) values (?, ?, ?, ?, ?)",
								new Object[] { new Date(), uid, fidold, fidnew,
										type });
				//
			} else if (type == 4) {
				this.jdbcTemplate
						.update("insert into History (timestamp, uid, type) values (?, ?, ?)",
								new Object[] { new Date(), uid, type });
			}
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Override
	public int createFile(File file) {
		int fid = createFileOnly(file);
		createHistory(file.getUid(), 0, fid, 1);
		return fid;
	}

	public int createFileOnly(File file) {
		// fid is an autoincrement integer
		// KeyHolder keyHolder = new GeneratedKeyHolder();
		// this.jdbcTemplate
		// .update("insert into File (fname, location, directory, hash, version, uid, isdir) values (?, ?, ?, ?, ?, ?, ?)",
		// new Object[] { file.getName(), file.getLocation(),
		// file.getParentDir(), file.getHash(),
		// file.getVersion(), file.getuId(), file.isDir() });
		// int ret = this.jdbcTemplate.queryForObject("select LAST_INSERT_ID()",
		// Integer.class);
		// return ret;

		final String INSERT_SQL = "insert into File (fname, location, directory, hash, hiscount, uid, isdir, ishidden) values (?, ?, ?, ?, ?, ?, ?, ?)";
		long autoGeneratedKey = -1;
		final File _file = file;
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			this.jdbcTemplate.update(new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(
						Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(
							INSERT_SQL, new String[] { "fid" });
					ps.setString(1, _file.getName());
					ps.setString(2, _file.getLocation());
					if (_file.getParentDir() <= 0) {
						ps.setNull(3, Types.NULL);
					} else {
						ps.setInt(3, _file.getParentDir());
					}
					ps.setString(4, _file.getHash());
					ps.setInt(5, _file.getReferenceCount());
					ps.setInt(6, _file.getUid());
					ps.setBoolean(7, _file.isDir());
					ps.setBoolean(8, false);
					return ps;
				}
			}, keyHolder);
			autoGeneratedKey = (Long) keyHolder.getKey();

		} catch (Exception ex) {
			return -1;
		}
		return (int) autoGeneratedKey;
	}

	@Override
	public boolean updateFile(File file) {
		try {
			// set old file invisible
			this.jdbcTemplate.update("update File set isHidden = ? where fid = ?",
					new Object[] { true, file.getFid() });
			// create new file
			int fid = createFileOnly(file);
			// write transaction to history
			createHistory(file.getUid(), file.getFid(), fid, 3);
			// set new fid to file
			file.setFid(fid);
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Override
	public File getFileByFid(int fid) {
		//will return hidden files
		File ret = (File) this.jdbcTemplate
				.queryForObject(
						"select fid, fname, location, directory, hash, hiscount, uid, isdir, ishidden from File where fid = ?",
						new Object[] { fid }, new FileMapper());
		return ret;
	}

	@Override
	public List<File> getFileByParentDir(File parentDir) {
		List<File> files = this.jdbcTemplate
				.query("select fid, fname, location, directory, hash, hiscount, uid, isdir, ishidden from File where directory = ?",
						new Object[] { parentDir.getFid() }, new FileMapper());
		return files;
	}

	@Override
	public boolean deleteFile(File file) {
		if (file.getFid() <= 0) {
			return false;
		}
		try {
			this.jdbcTemplate.update("update File set isHidden = ? where fid = ?",
					new Object[] { true, file.getFid() });
			createHistory(file.getUid(), file.getFid(), 0, 2);
		} catch (DataAccessException e) {
			return false;
		}
		return true;
	}

	@Override
	public File getRootDirForUser(int userId) {
		User u = (User) this.jdbcTemplate
				.queryForObject(
						"select uid, uname, password, email, rootfid from User where uid = ?",
						new Object[] { userId }, new UserMapper());
		int rootfid = u.getRootfid();
		return getFileByFid(rootfid);
	}

}
