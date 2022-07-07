package org.sjtu.server;

import com.sjtu.interfaces.UserService;

/**
 * @author hcstart
 * @create 2022-06-18 11:21
 */
public class UserServiceImpl implements UserService {
    @Override
    public void test() {
        System.out.println("this is a test");
    }
}
