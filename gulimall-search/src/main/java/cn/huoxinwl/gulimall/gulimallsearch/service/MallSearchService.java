package cn.huoxinwl.gulimall.gulimallsearch.service;

import cn.huoxinwl.gulimall.gulimallsearch.vo.SearchParam;
import cn.huoxinwl.gulimall.gulimallsearch.vo.SearchResult;

public interface MallSearchService {

    /**
     * @param param 检索的所有参数
     * @return  返回检索的结果，里面包含页面需要的所有信息
     */
    SearchResult search(SearchParam param);
}
