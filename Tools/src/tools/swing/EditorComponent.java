package tools.swing;

public interface EditorComponent
{
	/**
	 * @return false if the editor does not wish to close (editor should save itself if needed
	 */
	public boolean attemptClose();

	public boolean needsSaving();

	public boolean save();

}
