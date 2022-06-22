package cc.carm.lib.easysql.api;

import cc.carm.lib.easysql.api.action.PreparedSQLUpdateAction;
import cc.carm.lib.easysql.api.action.PreparedSQLUpdateBatchAction;
import cc.carm.lib.easysql.api.action.SQLUpdateAction;
import cc.carm.lib.easysql.api.action.SQLUpdateBatchAction;
import cc.carm.lib.easysql.api.builder.*;
import cc.carm.lib.easysql.api.function.SQLDebugHandler;
import cc.carm.lib.easysql.api.function.SQLExceptionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

/**
 * SQLManager 是EasySQL的核心类，用于管理数据库连接，提供数据库操作的方法。
 *
 * @author CarmJos
 */
public interface SQLManager {

    Logger getLogger();

    boolean isDebugMode();


    /**
     * 获取用于执行 {@link SQLAction#executeAsync()} 的线程池。
     * <br> 默认线程池为 {@link ThreadPoolExecutor} ,大小为 3。
     *
     * @return {@link ExecutorService}
     */
    @NotNull ExecutorService getExecutorPool();

    /**
     * 设定用于执行 {@link SQLAction#executeAsync()} 的线程池。
     *
     * @param executorPool {@link ExecutorService}
     */
    void setExecutorPool(@NotNull ExecutorService executorPool);


    /**
     * 设定是否启用调试模式。
     * 启用调试模式后，会在每次执行SQL语句时，调用 {@link #getDebugHandler()} 来输出调试信息。
     *
     * @param debugMode 是否启用调试模式
     */
    void setDebugMode(@NotNull Supplier<@NotNull Boolean> debugMode);

    /**
     * 设定是否启用调试模式。
     * 启用调试模式后，会在每次执行SQL语句时，调用 {@link #getDebugHandler()} 来输出调试信息。
     *
     * @param enable 是否启用调试模式
     */
    default void setDebugMode(boolean enable) {
        setDebugMode(() -> enable);
    }

    /**
     * 获取调试处理器，用于处理调试信息。
     *
     * @return {@link SQLDebugHandler}
     */
    @NotNull SQLDebugHandler getDebugHandler();

    /**
     * 设定调试处理器，默认为 {@link SQLDebugHandler#defaultHandler(Logger)} 。
     *
     * @param debugHandler {@link SQLDebugHandler}
     */
    void setDebugHandler(@NotNull SQLDebugHandler debugHandler);

    /**
     * 得到连接池源
     *
     * @return DataSource
     */
    @NotNull DataSource getDataSource();

    /**
     * 得到一个数据库连接实例
     *
     * @return Connection
     * @throws SQLException 见 {@link DataSource#getConnection()}
     */
    @NotNull Connection getConnection() throws SQLException;

    /**
     * 得到正使用的查询。
     *
     * @return 查询列表
     */
    @NotNull Map<UUID, SQLQuery> getActiveQuery();

    /**
     * 获取改管理器提供的默认异常处理器。
     * 若未使用过 {@link #setExceptionHandler(SQLExceptionHandler)} 方法，
     * 则默认返回 {@link SQLExceptionHandler#detailed(Logger)} 。
     *
     * @return {@link SQLExceptionHandler}
     */
    @NotNull SQLExceptionHandler getExceptionHandler();

    /**
     * 设定通用的异常处理器。
     * <br> 在使用 {@link SQLAction#execute(SQLExceptionHandler)} 等相关方法时，若传入的处理器为null，则会采用此处理器。
     * <br> 若该方法传入参数为 null，则会使用 {@link SQLExceptionHandler#detailed(Logger)} 。
     *
     * @param handler 异常处理器
     */
    void setExceptionHandler(@Nullable SQLExceptionHandler handler);

    /**
     * 执行一条不需要返回结果的SQL语句(多用于UPDATE、REPLACE、DELETE方法)
     * 该方法使用 Statement 实现，请注意SQL注入风险！
     *
     * @param sql SQL语句内容
     * @return 更新的行数
     * @see SQLUpdateAction
     */
    @Nullable Integer executeSQL(String sql);

    /**
     * 执行一条不需要返回结果的预处理SQL更改(UPDATE、REPLACE、DELETE)
     *
     * @param sql    SQL语句内容
     * @param params SQL语句中 ? 的对应参数
     * @return 更新的行数
     * @see PreparedSQLUpdateAction
     */
    @Nullable Integer executeSQL(String sql, Object[] params);

    /**
     * 执行多条不需要返回结果的SQL更改(UPDATE、REPLACE、DELETE)
     *
     * @param sql         SQL语句内容
     * @param paramsBatch SQL语句中对应?的参数组
     * @return 对应参数返回的行数
     * @see PreparedSQLUpdateBatchAction
     */
    @Nullable List<Integer> executeSQLBatch(String sql, Iterable<Object[]> paramsBatch);


    /**
     * 执行多条不需要返回结果的SQL。
     * 该方法使用 Statement 实现，请注意SQL注入风险！
     *
     * @param sql     SQL语句内容
     * @param moreSQL 更多SQL语句内容
     * @return 对应参数返回的行数
     * @see SQLUpdateBatchAction
     */
    @Nullable List<Integer> executeSQLBatch(@NotNull String sql, String... moreSQL);

    /**
     * 执行多条不需要返回结果的SQL。
     *
     * @param sqlBatch SQL语句内容
     * @return 对应参数返回的行数
     */
    @Nullable List<Integer> executeSQLBatch(@NotNull Iterable<String> sqlBatch);

    /**
     * 在库中创建一个表
     *
     * @param tableName 表名
     * @return {@link TableCreateBuilder}
     */
    TableCreateBuilder createTable(@NotNull String tableName);

    /**
     * 对库中的某个表执行更改
     *
     * @param tableName 表名
     * @return {@link TableAlterBuilder}
     */
    TableAlterBuilder alterTable(@NotNull String tableName);

    /**
     * 新建一个查询
     *
     * @return {@link QueryBuilder}
     */
    QueryBuilder createQuery();

    /**
     * 创建一条插入操作
     *
     * @param tableName 目标表名
     * @return {@link InsertBuilder}
     */
    InsertBuilder<PreparedSQLUpdateAction<Integer>> createInsert(@NotNull String tableName);

    /**
     * 创建支持多组数据的插入操作
     *
     * @param tableName 目标表名
     * @return {@link InsertBuilder}
     */
    InsertBuilder<PreparedSQLUpdateBatchAction<Integer>> createInsertBatch(@NotNull String tableName);

    /**
     * 创建一条替换操作
     *
     * @param tableName 目标表名
     * @return {@link ReplaceBuilder}
     */
    ReplaceBuilder<PreparedSQLUpdateAction<Integer>> createReplace(@NotNull String tableName);

    /**
     * 创建支持多组数据的替换操作
     *
     * @param tableName 目标表名
     * @return {@link ReplaceBuilder}
     */
    ReplaceBuilder<PreparedSQLUpdateBatchAction<Integer>> createReplaceBatch(@NotNull String tableName);

    /**
     * 创建更新操作
     *
     * @param tableName 目标表名
     * @return {@link UpdateBuilder}
     */
    UpdateBuilder createUpdate(@NotNull String tableName);

    /**
     * 创建删除操作
     *
     * @param tableName 目标表名
     * @return {@link DeleteBuilder}
     */
    DeleteBuilder createDelete(@NotNull String tableName);

}
