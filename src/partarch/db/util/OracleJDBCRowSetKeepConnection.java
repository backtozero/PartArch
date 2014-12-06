/*
 * Copyright (C) 2014 <gotozero@yandex.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package partarch.db.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import oracle.jdbc.rowset.OracleJDBCRowSet;


/**
 * Override method call() for disable Connection.close
 * @author <gotozero@yandex.com>
 */
public class OracleJDBCRowSetKeepConnection extends OracleJDBCRowSet {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    
    public OracleJDBCRowSetKeepConnection() throws SQLException {
        super();
    }

    public OracleJDBCRowSetKeepConnection(Connection cnctn) throws SQLException {
        super(cnctn);
    }
    
    


    @Override
    public void close() throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
        if (preparedStatement != null) {
            preparedStatement.close();
        }
    }

}
