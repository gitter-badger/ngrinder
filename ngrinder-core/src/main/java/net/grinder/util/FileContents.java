//Copyright (C) 2004 - 2008 Philip Aston
//All rights reserved.
//
//This file is part of The Grinder software distribution. Refer to
//the file LICENSE which is part of The Grinder distribution for
//licensing details. The Grinder distribution is available on the
//Internet at http://grinder.sourceforge.net/
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
//STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
//OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import net.grinder.common.Closer;
import net.grinder.common.GrinderException;
import net.grinder.common.UncheckedInterruptedException;

/**
* Pairing of relative filename and file contents.
* Add constructor for setting relative root directory
*
* @author Philip Aston
* @author Gisoo Gwon
*/
public final class FileContents implements Serializable {

	private static final long serialVersionUID = -3344373206447488299L;

	/** @serial The file name. */
	private final File m_filename;

	/** @serial The file data. */
	private final byte[] m_contents;
	
	/**
	* Constructor. Builds a FileContents from local file system.
	*
	* @param baseDirectory Base directory used to resolve relative filenames.
	* @param file Relative filename.
	* @exception FileContentsException If an error occurs.
	*/
	public FileContents(File baseDirectory, File file) throws FileContentsException {
		this(baseDirectory, file, null);
	}

	/**
	* Constructor. Builds a FileContents from local file system.
	* <p>
	* Actual file path is "baseDirectory/file"<br>
	* But FileContents.file is pointing virtual path "baseDirectory/relativeRootname/file"
	* </p>
	*
	* @param baseDirectory Base directory used to resolve relative filenames.
	* @param file Relative filename.
	* @param relativeRootname The relative root directory name(virtual path)
	* @exception FileContentsException If an error occurs.
	*/
	public FileContents(File baseDirectory, File file, String relativeRootname) throws FileContentsException {

		if (file.isAbsolute()) {
			throw new FileContentsException("Original file name '" + file + "' is not relative");
		}

		String rootname = relativeRootname != null ? relativeRootname + File.separator : "";
		m_filename = new File(rootname + file.getPath());

		final File localFile = new File(baseDirectory, file.getPath());

		final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

		try {
			new StreamCopier(4096, true).copy(new FileInputStream(localFile), byteOutputStream);
		} catch (IOException e) {
			UncheckedInterruptedException.ioException(e);
			throw new FileContentsException("Failed to read file: " + e.getMessage(), e);
		}

		m_contents = byteOutputStream.toByteArray();
	}

	/**
	* Allow unit tests access to the relative file name.
	*
	* @return The file name.
	*/
	File getFilename() {
		return m_filename;
	}

	/**
	* Allow unit tests access to the file contents.
	*
	* @return a <code>byte[]</code> value
	*/
	byte[] getContents() {
		return m_contents;
	}

	/**
	* Write the <code>FileContents</code> to the given directory,
	* overwriting any existing content.
	*
	* @param baseDirectory The base directory.
	* @exception FileContentsException If an error occurs.
	*/
	public void create(Directory baseDirectory) throws FileContentsException {

		final File localFile = baseDirectory.getFile(getFilename());

		localFile.getParentFile().mkdirs();

		OutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream(localFile);
			outputStream.write(getContents());
		} catch (IOException e) {
			UncheckedInterruptedException.ioException(e);
			throw new FileContentsException("Failed to create file: " + e.getMessage(), e);
		} finally {
			Closer.close(outputStream);
		}
	}

	/**
	* Return a description of the <code>FileContents</code>.
	*
	* @return The description.
	*/
	public String toString() {
		return "\"" + getFilename() + "\" (" + getContents().length + " bytes)";
	}

	/**
	* Exception that indicates a <code>FileContents</code> related
	* problem.
	*/
	public static final class FileContentsException extends GrinderException {
		
		private static final long serialVersionUID = -1329826318361005382L;

		FileContentsException(String message) {
			super(message);
		}

		FileContentsException(String message, Throwable nested) {
			super(message, nested);
		}
	}
}
