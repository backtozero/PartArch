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

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import partarch.model.Partition;

/**
 * 
 * @author <gotozero@yandex.com>
 */
public class ExchangePartition {

    

    private ObservableList<Partition> partitions;
    private String validation;
    private String updateIndexes;
    private String includingIndexes;
    private String tableLockType;
    private String sourceTable;
    private String intermediateTable;
    private String destinationTable;

    public ExchangePartition() {
    }

    public ExchangePartition(ObservableList<Partition> partitions, String validation, String updateIndexes, String includingIndexes,
            String tableLockType, String sourceTable, String intermediateTable, String destinationTable) {
        this.partitions = partitions;
        this.validation = validation;
        this.updateIndexes = updateIndexes;
        this.includingIndexes = includingIndexes;
        this.tableLockType = tableLockType;
        this.sourceTable = sourceTable;
        this.intermediateTable = intermediateTable;
        this.destinationTable = destinationTable;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        
        if (partitions != null) {
            getPartitions().stream().forEach((partition) -> {
                if (partition.getChecked()) {
                    buffer.append("-- " + sourceTable
                            + (intermediateTable != null ? " ---> " + intermediateTable : "")
                            + (destinationTable != null ? " ---> " + destinationTable : "")
                            + "\n");
                    buffer.append(tableLockType != null ? "LOCK TABLE " + sourceTable
                            + " PARTITION (" + partition.getPartitionName() + ") IN "
                            + tableLockType + ";\n" : "");
                    buffer.append(intermediateTable != null ? "ALTER TABLE " + sourceTable
                            + " EXCHANGE PARTITION " + partition.getPartitionName() + " WITH TABLE "
                            + intermediateTable + (includingIndexes != null ? " " + includingIndexes : "")
                            + (validation != null ? " " + validation : "")
                            + (updateIndexes != null ? " " + updateIndexes : "")
                            + ";\n" : "");
                    buffer.append(destinationTable != null ? "ALTER TABLE " + destinationTable
                            + " EXCHANGE PARTITION " + partition.getPartitionName() + " WITH TABLE "
                            + intermediateTable + (includingIndexes != null ? " " + includingIndexes : "")
                            + (validation != null ? " " + validation : "")
                            + (updateIndexes != null ? " " + updateIndexes : "")
                            + ";\n" : "");

                    buffer.append("\n");
                }
            });
        }

        return buffer.toString();
    }

    public List<String> getList() {
        List<String> list = new ArrayList<>();        
        if (partitions != null) {
            getPartitions().stream().forEach((partition) -> {
                if (partition.getChecked()) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("-- " + sourceTable
                            + (intermediateTable != null ? " ---> " + intermediateTable : "")
                            + (destinationTable != null ? " ---> " + destinationTable : "")
                            + "\n");
                    buffer.append(tableLockType != null ? "LOCK TABLE " + sourceTable
                            + " PARTITION (" + partition.getPartitionName() + ") IN "
                            + tableLockType + ";\n" : "");
                    buffer.append(intermediateTable != null ? "ALTER TABLE " + sourceTable
                            + " EXCHANGE PARTITION " + partition.getPartitionName() + " WITH TABLE "
                            + intermediateTable + (includingIndexes != null ? " " + includingIndexes : "")
                            + (validation != null ? " " + validation : "")
                            + (updateIndexes != null ? " " + updateIndexes : "")
                            + ";\n" : "");
                    buffer.append(destinationTable != null ? "ALTER TABLE " + destinationTable
                            + " EXCHANGE PARTITION " + partition.getPartitionName() + " WITH TABLE "
                            + intermediateTable + (includingIndexes != null ? " " + includingIndexes : "")
                            + (validation != null ? " " + validation : "")
                            + (updateIndexes != null ? " " + updateIndexes : "")
                            + ";\n" : "");

                    buffer.append("\n");
                    list.add(buffer.toString());
                }
                
            });
        }
        return list;
    }

    public ObservableList<Partition> getPartitions() {
        return partitions;
    }

    public String getValidation() {
        return validation;
    }

    public String getUpdateIndexes() {
        return updateIndexes;
    }

    public String getIncludingIndexes() {
        return includingIndexes;
    }

    public String getTableLockType() {
        return tableLockType;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public String getIntermediateTable() {
        return intermediateTable;
    }

    public String getDestinationTable() {
        return destinationTable;
    }

    public ExchangePartition setPartitions(ObservableList<Partition> partitions) {
        this.partitions = partitions;
        return this;
    }

    public ExchangePartition setValidation(String validation) {
        this.validation = validation;
        return this;
    }

    public ExchangePartition setUpdateIndexes(String updateIndexes) {
        this.updateIndexes = updateIndexes;
        return this;
    }

    public ExchangePartition setIncludingIndexes(String includingIndexes) {
        this.includingIndexes = includingIndexes;
        return this;
    }

    public ExchangePartition setTableLockType(String tableLockType) {
        this.tableLockType = tableLockType;
        return this;
    }

    public ExchangePartition setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
        return this;
    }

    public ExchangePartition setIntermediateTable(String intermediateTable) {
        this.intermediateTable = intermediateTable;
        return this;
    }

    public ExchangePartition setDestinationTable(String destinationTable) {
        this.destinationTable = destinationTable;
        return this;
    }

}
