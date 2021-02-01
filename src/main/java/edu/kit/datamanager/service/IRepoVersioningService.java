package edu.kit.datamanager.service;

import edu.kit.datamanager.entities.VersionInfo;
import edu.kit.datamanager.configuration.RepoBaseConfiguration;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public interface IRepoVersioningService{
  /** 
   * Configure the service.
   */
  void configure(RepoBaseConfiguration applicationProperties);

  /**
   * adds the file of an object to the default OCFL repository.
   *
   * @param resourceId identifier of the object
   * @param callerId name of the user
   * @param path path of the file
   * @param data the file
   * @param options contains three keys: finalize, token number and parent.
   * @return OcflObjectIdentifier : resourceId, versionId, parent version,
   * finalize, token
   */
  void write(String resourceId, String callerId, String path, InputStream data, Map<String, String> options);

  /**
   * returns files of an object's version.
   *
   * @param resourceId
   * @param callerId
   * @param path
   * @param versionId
   * @param destination
   * @param options
   * @return
   */
  void read(String resourceId, String callerId, String path, String versionId, OutputStream destination, Map<String, String> options);

  /**
   * returns information for a specific resource
   *
   * @param resourceId
   * @param path
   * @param versionId
   * @param options
   * @return
   */
  VersionInfo info(String resourceId, String path, String versionId, Map<String, String> options);

  /**
   * Returns the name of this versioning service. The name should be unique.
   * Otherwise, a random implementation with the provided name will be used.
   *
   * @return The service name.
   */
  String getServiceName();
}
