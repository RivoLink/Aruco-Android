package min3d.interfaces;

public interface IDirtyManaged 
{
	public boolean isDirty();
	public void setDirtyFlag();
	public void clearDirtyFlag();
}
