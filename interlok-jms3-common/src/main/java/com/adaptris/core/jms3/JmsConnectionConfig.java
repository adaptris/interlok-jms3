/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.jms3;


/**
 * Simple interface that provides configuration information for sub components.
 *
 */
public interface JmsConnectionConfig {

  /**
   * The client id.
   *
   * @return the client id
   */
  String configuredClientId();

  /**
   * The password.
   *
   * @return the password
   */
  String configuredPassword();

  /**
   * The username.
   *
   * @return the username
   */
  String configuredUserName();

  /**
   * The vendor specific implementation.
   *
   * @return the vendor specific implementation
   */
  <T extends VendorImplementationBase> T configuredVendorImplementation();

}
