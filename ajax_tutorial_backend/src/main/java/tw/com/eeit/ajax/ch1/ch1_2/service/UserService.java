package tw.com.eeit.ajax.ch1.ch1_2.service;

import java.util.List;

import org.springframework.stereotype.Service;

import tw.com.eeit.ajax.ch1.ch1_2.model.User;

@Service
public class UserService {

    public List<User> findAll() {
        return List.of(
                new User(1, "王小明", List.of("Java", "Spring Boot"),
                        List.of(new User.UserExperience("Google", "後端工程師"))),
                new User(2, "李小美", List.of("JavaScript", "Vue.js", "React"),
                        List.of(new User.UserExperience("Meta", "前端工程師"))),
                new User(3, "張志豪", List.of("Python", "Django", "Flask"),
                        List.of(new User.UserExperience("Amazon", "資料科學家"))),
                new User(4, "陳雅婷", List.of("UI/UX", "Figma", "Sketch"),
                        List.of(new User.UserExperience("Apple", "產品設計師"))),
                new User(5, "林建國", List.of("C#", ".NET Core"),
                        List.of(new User.UserExperience("Microsoft", "全端工程師"))),
                new User(6, "黃淑芬", List.of("Kotlin", "Android"),
                        List.of(new User.UserExperience("Netflix", "行動裝置開發者"))),
                new User(7, "吳志偉", List.of("Swift", "iOS"),
                        List.of(new User.UserExperience("Spotify", "iOS 開發者"))),
                new User(8, "劉以柔", List.of("Docker", "Kubernetes", "AWS"),
                        List.of(new User.UserExperience("Uber", "DevOps 工程師"))),
                new User(9, "蔡宗翰", List.of("SQL", "PostgreSQL", "Redis"),
                        List.of(new User.UserExperience("Oracle", "資料庫管理員"))),
                new User(10, "楊佩珊", List.of("Go", "Microservices"),
                        List.of(new User.UserExperience("Twitter", "後端工程師"))),
                new User(11, "許家豪", List.of("Rust", "WebAssembly"),
                        List.of(new User.UserExperience("Mozilla", "系統工程師"))),
                new User(12, "鄭百合", List.of("PHP", "Laravel"),
                        List.of(new User.UserExperience("Airbnb", "網頁開發者"))),
                new User(13, "謝博文", List.of("Ruby", "Ruby on Rails"),
                        List.of(new User.UserExperience("Shopify", "後端工程師"))),
                new User(14, "曾欣怡", List.of("TypeScript", "Angular"),
                        List.of(new User.UserExperience("IBM", "企業級應用開發者"))),
                new User(15, "賴冠宇", List.of("Scala", "Akka", "Spark"),
                        List.of(new User.UserExperience("LinkedIn", "大數據工程師"))),
                new User(16, "蘇郁婷", List.of("C++", "Qt"),
                        List.of(new User.UserExperience("Adobe", "軟體工程師"))),
                new User(17, "莊偉哲", List.of("Unity", "C#"),
                        List.of(new User.UserExperience("Blizzard", "遊戲開發者"))),
                new User(18, "江怡君", List.of("Machine Learning", "TensorFlow"),
                        List.of(new User.UserExperience("OpenAI", "AI 研究員"))),
                new User(19, "羅志明", List.of("Cybersecurity", "Ethical Hacking"),
                        List.of(new User.UserExperience("CrowdStrike", "資安工程師"))),
                new User(20, "梁雅雯", List.of("Selenium", "JUnit"),
                        List.of(new User.UserExperience("Salesforce", "測試工程師"))));
    }

}
