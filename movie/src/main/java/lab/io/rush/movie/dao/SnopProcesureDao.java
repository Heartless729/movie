package lab.io.rush.movie.dao;

/**
 * 抢购事务持久层
 * */
public interface SnopProcesureDao {
	/**
     * 调用抢购存储过程
     *
     * @param uid     用户编号
     * @param movieId 电影票编号
     * @param num     抢购数量
     * @return 抢购状态
     */
	int callSnopProc(int uid,int movieId,int num);
}
