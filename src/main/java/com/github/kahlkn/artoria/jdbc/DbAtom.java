package com.github.kahlkn.artoria.jdbc;

import java.sql.SQLException;

/**
 * Db atom.
 * @author Kahle
 */
public interface DbAtom {

    /**
     * Code run in atom.
     * @return Run success or failure
     * @throws SQLException Sql run error
     */
    boolean run() throws SQLException;

}
