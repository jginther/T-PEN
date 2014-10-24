/**This code came from http://www.javabeat.net/articles/262-asynchronous-file-upload-using-ajax-jquery-progress-ba-1.html*/

package ImageUpload;


import org.apache.commons.fileupload.ProgressListener;
	public class FileUploadListener implements ProgressListener{
		private volatile long bytesRead = 0L, contentLength = 0L, item = 0L;
		public FileUploadListener() {
			super();
		}

		public void update(long aBytesRead, long aContentLength, int anItem) {
			bytesRead = aBytesRead;
			contentLength = aContentLength;
			item = anItem;
		}

		public long getBytesRead() {
			return bytesRead;
		}

		public long getContentLength() {
			return contentLength;
		}

		public long getItem() {
			return item;
		}
	}
