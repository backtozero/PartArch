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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author <gotozero@yandex.com>
 */
public class PrepareSQL {
    
    private String statement;
    
    
    /**
     *
     * @param sqlQuery
     * @param parameters
     * 
     */
    public PrepareSQL(String sqlQuery, Object... parameters) {

        statement = generateActualSql(sqlQuery, parameters);
        
    }
    
    @Override
    public String toString() {
        return statement;
    }
    
  private String generateActualSql(String sqlQuery, Object... parameters) {
    String[] parts = sqlQuery.split("\\?");
    StringBuilder sb = new StringBuilder();

    // This might be wrong if some '?' are used as litteral '?'
    for (int i = 0; i < parts.length; i++) {
        String part = parts[i];
        sb.append(part);
        if (i < parameters.length) {
            sb.append(formatParameter(parameters[i]));
        }
    }

    return sb.toString();
}

private String formatParameter(Object parameter) {
    if (parameter == null) {
        return "NULL";
    } else {
        if (parameter instanceof String) {
            return "'" + ((String) parameter).replace("'", "''") + "'";
        } else if (parameter instanceof Timestamp) {
            return "to_timestamp('" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS").
                    format(parameter) + "', 'mm/dd/yyyy hh24:mi:ss.ff3')";
        } else if (parameter instanceof Date) {
            return "to_date('" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").
                    format(parameter) + "', 'mm/dd/yyyy hh24:mi:ss')";
        } else if (parameter instanceof Boolean) {
            return ((Boolean) parameter).booleanValue() ? "1" : "0";
        } else {
            return parameter.toString();
        }
    }
}  
    
}
