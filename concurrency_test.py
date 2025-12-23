import requests
import threading
import time
import json
import random

# 配置
BASE_URL = "http://localhost:8080"
THREAD_COUNT = 20 # 模拟 20 个并发请求
STOCK_COUNT = 5   # 书籍初始库存为 5

# 统计结果
success_count = 0
fail_count = 0
lock = threading.Lock()

def create_test_user():
    """创建一个测试用户"""
    url = f"{BASE_URL}/api/auth/register"
    headers = {"Content-Type": "application/json"}
    phone = f"138{random.randint(10000000, 99999999)}"
    data = {
        "username": f"TestUser{random.randint(1000, 9999)}",
        "phone": phone,
        "password": "Password123"
    }
    try:
        response = requests.post(url, json=data)
        if response.status_code == 200 and response.json()['code'] == 200:
            user_data = response.json()['data']
            # AuthService.register 直接返回 userId
            user_id = user_data.get('userId')
            
            print(f"✅ 测试用户创建成功: ID={user_id}, Phone={phone}")
            return user_id
        else:
            print(f"❌ 创建用户失败: {response.text}")
            return None
    except Exception as e:
        print(f"❌ 连接服务器失败: {e}")
        return None

def create_test_book():
    """创建一个测试用的书籍，库存设为 STOCK_COUNT"""
    url = f"{BASE_URL}/api/books/save"
    headers = {"Content-Type": "application/json"}
    data = {
        "bookName": "并发测试专用书",
        "author": "TestBot",
        "category": "T",
        "price": 10.0,
        "stock": STOCK_COUNT,
        "status": 1,
        "description": "用于测试并发控制",
        "publisher": "Test Press"
    }
    try:
        response = requests.post(url, json=data)
        if response.status_code == 200 and response.json()['code'] == 200:
            book = response.json()['data']
            print(f"✅ 测试书籍创建成功: ID={book['bookId']}, 库存={book['stock']}")
            return book['bookId']
        else:
            print(f"❌ 创建书籍失败: {response.text}")
            return None
    except Exception as e:
        print(f"❌ 连接服务器失败: {e}")
        return None

def buy_book(user_id, book_id, thread_id):
    """尝试购买书籍"""
    global success_count, fail_count
    url = f"{BASE_URL}/api/orders/create"
    headers = {"Content-Type": "application/json"}
    data = {
        "userId": user_id,
        "bookId": book_id,
        "quantity": 1
    }
    
    try:
        response = requests.post(url, json=data)
        result = response.json()
        
        with lock:
            if response.status_code == 200 and result['code'] == 200:
                success_count += 1
                print(f"线程-{thread_id}: 抢购成功 🎉")
            else:
                fail_count += 1
                msg = result.get('message', '未知错误')
                print(f"线程-{thread_id}: 抢购失败 ❌ ({msg})")
    except Exception as e:
        with lock:
            fail_count += 1
            print(f"线程-{thread_id}: 请求异常 ⚠️ ({e})")

def check_final_stock(book_id):
    """检查最终库存"""
    url = f"{BASE_URL}/api/books/{book_id}"
    try:
        response = requests.get(url)
        if response.status_code == 200:
            book = response.json()['data']
            print(f"\n📊 最终库存检查: ID={book_id}, 剩余库存={book['stock']}, 销量={book['sales']}")
            return book['stock']
    except Exception as e:
        print(f"查询库存失败: {e}")
        return -1

def main():
    print("🚀 开始并发测试...")
    
    # 1. 准备数据
    user_id = create_test_user()
    if not user_id:
        print("无法创建测试用户，终止测试")
        return

    book_id = create_test_book()
    if not book_id:
        print("无法继续测试，请确保后端服务已启动 (localhost:8080)")
        return

    print(f"\n🔥 模拟 {THREAD_COUNT} 个用户同时抢购 {STOCK_COUNT} 本书...")
    time.sleep(1)

    # 2. 发起并发请求
    threads = []
    start_time = time.time()
    
    for i in range(THREAD_COUNT):
        t = threading.Thread(target=buy_book, args=(user_id, book_id, i+1))
        threads.append(t)
        t.start()
    
    for t in threads:
        t.join()
        
    end_time = time.time()
    
    # 3. 输出结果
    print("\n" + "="*30)
    print(f"测试耗时: {end_time - start_time:.2f} 秒")
    print(f"计划抢购: {THREAD_COUNT} 次")
    print(f"成功次数: {success_count} (预期: {STOCK_COUNT})")
    print(f"失败次数: {fail_count} (预期: {THREAD_COUNT - STOCK_COUNT})")
    
    # 4. 验证库存
    final_stock = check_final_stock(book_id)
    
    print("="*30)
    if success_count == STOCK_COUNT and final_stock == 0:
        print("✅ 测试通过！并发控制生效，未发生超卖。")
    elif success_count > STOCK_COUNT:
        print("❌ 测试失败！发生超卖现象！")
    else:
        print("⚠️ 测试结果需人工确认 (可能是其他错误导致失败)")

if __name__ == "__main__":
    main()
