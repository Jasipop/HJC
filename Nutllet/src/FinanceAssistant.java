import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class FinanceAssistant extends JFrame {
    // 颜色常量
    private static final Color DARK_PURPLE = new Color(75, 0, 130); // #4B0082
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY_BG = new Color(241, 241, 241);

    public FinanceAssistant() {
        // 窗口设置
        setTitle("Smart Finance Assistant");
        setSize(1200, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(WHITE);
        setLayout(new BorderLayout(10, 10));

        // 添加组件
        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createBottomNav(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // 顶部导航栏
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_PURPLE);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        // 左侧标题
        JLabel titleLabel = new JLabel("NUTLLET");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(WHITE);

        // 企业版按钮
        JButton enterpriseBtn = createHeaderButton("Enterprise Edition");
        enterpriseBtn.setBackground(WHITE);
        enterpriseBtn.setForeground(DARK_PURPLE);
        enterpriseBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 2, WHITE),
            BorderFactory.createEmptyBorder(2, 15, 2, 15)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(DARK_PURPLE);
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createHorizontalStrut(15));
        leftPanel.add(enterpriseBtn);

        // 右侧功能按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(DARK_PURPLE);
        for (String text : new String[]{"Syncing", "Logout", "Help"}) {
            rightPanel.add(createHeaderButton(text));
        }

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(WHITE);
        btn.setBackground(DARK_PURPLE);
        btn.setBorder(BorderFactory.createLineBorder(WHITE));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        return btn;
    }

    // 主内容区域
    private JSplitPane createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(650);
        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(createRightPanel());
        splitPane.setBorder(null);
        return splitPane;
    }

    // 左侧面板
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setBackground(WHITE);

        // 当前余额
        JLabel balance = new JLabel("<html><font size='4'>Current Balance</font><br><font size='6'>¥ 8888.88</font></html>");
        balance.setFont(new Font("Segoe UI", Font.BOLD, 24));
        balance.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(balance);

        // 饼图
        panel.add(createPieChart());
        panel.add(Box.createVerticalStrut(20));

        // 进度模块
        panel.add(createProgressSection("Loan Repayment Reminder", 20));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createProgressSection("Month Budget", 65));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createProgressSection("Present Planning", 30));
        panel.add(Box.createVerticalStrut(20));

        // 操作按钮
        panel.add(createActionButton("Modify Your Reminder"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createActionButton("More Details"));
        
        return panel;
    }

    // 右侧面板
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 0, 20, 30));
        panel.setBackground(WHITE);

        // 标题栏
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(new EmptyBorder(0, 30, 15, 30));
        
        JLabel title = new JLabel("Recent Consumption");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        JTextField searchField = new JTextField("Search...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setForeground(Color.GRAY);
        searchField.setPreferredSize(new Dimension(150, 30));
        
        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(searchField, BorderLayout.EAST);
        panel.add(titlePanel, BorderLayout.NORTH);

        // 消费记录列表
        String[][] transactions = {
            {"8:08 PM", "Catering", "¥451", "Feb 20 2015"},
            {"5:02 PM", "Living", "¥200", "Feb 20 2015"},
            {"4:53 PM", "Catering", "¥10", "Feb 20 2015"},
            {"4:36 PM", "Entertainment", "¥63", "Feb 20 2015"},
            {"8:21 PM", "Entertainment", "¥50", "Feb 25 2025"},
            {"7:28 PM", "Living", "¥200", "Feb 24 2005"},
            {"6:27 PM", "Catering", "¥16", "Feb 24 2025"}
        };
        
        JList<String> list = new JList<>(new AbstractListModel<String>() {
            public int getSize() { return transactions.length; }
            public String getElementAt(int i) { 
                return String.format("<html><div style='padding:8px;'>"
                    + "<b>%s %s</b><br>"
                    + "<font color='#666'>%s • %s</font></div>",
                    transactions[i][0], transactions[i][1], 
                    transactions[i][2], transactions[i][3]);
            }
        });
        
        list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(8, 15, 8, 15));
                label.setBackground(isSelected ? GRAY_BG : WHITE);
                return label;
            }
        });
        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        list.setBackground(WHITE);
        
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    // 底部导航栏（新增）
    private JPanel createBottomNav() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        navPanel.setBackground(DARK_PURPLE);
        navPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        for (String text : new String[]{"Home", "Profile", "Settings"}) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setForeground(WHITE);
            btn.setBackground(DARK_PURPLE);
            btn.setBorder(BorderFactory.createLineBorder(WHITE));
            btn.setFocusPainted(false);
            navPanel.add(btn);
        }
        return navPanel;
    }

    // 饼图生成
    private ChartPanel createPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Shopping", 25);
        dataset.setValue("Catering", 35);
        dataset.setValue("Living", 15);
        dataset.setValue("Sports", 15);
        dataset.setValue("Exercise", 10);

        JFreeChart chart = ChartFactory.createPieChart(null, dataset, false, false, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Shopping", new Color(255, 153, 153));
        plot.setSectionPaint("Catering", new Color(102, 204, 255));
        plot.setSectionPaint("Living", new Color(153, 255, 153));
        plot.setSectionPaint("Sports", new Color(255, 204, 153));
        plot.setSectionPaint("Exercise", new Color(204, 153, 255));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));
        return chartPanel;
    }

    // 进度条组件
    private JPanel createProgressSection(String title, int progress) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(WHITE);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setValue(progress);
        progressBar.setStringPainted(true);
        progressBar.setForeground(DARK_PURPLE);
        progressBar.setBackground(GRAY_BG);
        progressBar.setString(String.format("%d%%", progress));
        
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(progressBar);
        return panel;
    }

    // 功能按钮样式
    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(WHITE);
        button.setBackground(DARK_PURPLE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 30, 10, 30)
        ));
        button.setMaximumSize(new Dimension(250, 40));
        return button;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new FinanceAssistant();
    }
}