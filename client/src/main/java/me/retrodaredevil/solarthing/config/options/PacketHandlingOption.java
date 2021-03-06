package me.retrodaredevil.solarthing.config.options;


import java.io.File;
import java.util.List;

public interface PacketHandlingOption extends TimeZoneOption {

	List<File> getDatabaseConfigurationFiles();

	String getSourceId();
	int getFragmentId();

	Integer getUniqueIdsInOneHour();

	List<ExtraOptionFlag> getExtraOptionFlags();
}
