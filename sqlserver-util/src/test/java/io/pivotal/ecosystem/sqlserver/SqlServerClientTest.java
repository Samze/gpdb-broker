/**
 * Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.ecosystem.sqlserver;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Slf4j
public class SqlServerClientTest {

    @Autowired
    private SqlServerClient client;

    @Test
    public void testSAConnection() throws SQLException {
        Connection c = client.getSAConnection();
        assertNotNull(c);
        c.close();
    }

    @Test
    @Ignore
    public void testCreateAndDeleteDatabase() throws Exception {
//        String dbName = String.valueOf(System.currentTimeMillis());
//        assertFalse(client.checkDatabaseExists(dbName));
        String db = client.createDatabase();

        Map<String, String> userCredentials = client.createUserCreds(db);
        assertTrue(client.checkDatabaseExists(db));

        String uid = userCredentials.get(SqlServerClient.USERNAME);
        assertNotNull(uid);

        String pw = userCredentials.get(SqlServerClient.PASSWORD);
        assertNotNull(pw);

        assertEquals(db, userCredentials.get(SqlServerClient.DATABASE));

        Connection c = client.getUserConnection(uid, pw, db);
        assertNotNull(c);

        c.close();

        //how about a data source?
//        SQLServerConnectionPoolDataSource dataSource = new SQLServerConnectionPoolDataSource();

        BasicDataSource dataSource = new BasicDataSource();
        String s = SQLServerDriver.class.getName();
//        datasource.

        dataSource.setDriverClassName(s);

//        dataSource.setServerName(client.getHost());
//        dataSource.setPortNumber(client.getPort());
//        dataSource.setDatabaseName(db);

//        String url = client.getDbUrl() + ";databaseName=" + db + ", " +  userCredentials.get(SqlServerClient.USERNAME) + ", " + userCredentials.get(SqlServerClient.PASSWORD);
//        String url = client.getDbUrl() + "/"+ db ;

//        String url = client.getDbUrl() + ";databaseName=" + db;

        String url = client.getDbUrl();
        dataSource.setUrl(url);

        dataSource.setUsername(userCredentials.get(SqlServerClient.USERNAME));
        dataSource.setPassword(userCredentials.get(SqlServerClient.PASSWORD));
        c = dataSource.getConnection();
        assertNotNull(c);
        c.close();

        assertTrue(client.checkUserExists(userCredentials.get(SqlServerClient.USERNAME)));
        client.deleteUserCreds(userCredentials.get(SqlServerClient.USERNAME));
        assertFalse(client.checkUserExists(userCredentials.get(SqlServerClient.USERNAME)));

        client.deleteDatabase(db);
        assertFalse(client.checkDatabaseExists(db));
    }
}