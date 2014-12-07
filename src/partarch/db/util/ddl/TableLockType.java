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
package partarch.db.util.ddl;

/**
 * 
 * @author <gotozero@yandex.com>
 */
public interface TableLockType {

    public static final String LOCK_MODE_SHARE = new String("SHARE MODE");
    public static final String LOCK_MODE_EXCLUSIVE_NOWAIT = new String("EXCLUSIVE MODE NOWAIT");
    public static final String LOCK_MODE_EXCLUSIVE = new String("EXCLUSIVE MODE");

}
