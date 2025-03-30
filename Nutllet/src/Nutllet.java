import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.border.MatteBorder;
import javax.swing.border.CompoundBorder;

public class Nutllet extends JFrame {
    // 精确颜色匹配
    private static final Color PRIMARY_PURPLE = new Color(128, 100, 228);
    private static final Color LIGHT_PURPLE_BG = new Color(245, 241, 255);
    private static final Color TEXT_SECONDARY = new Color(102, 102, 102);

    public Nutllet() {
        setTitle("NUTLLET - Financial Management");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createAdvicePanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // 精确还原顶部导航栏
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_PURPLE);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        // 左侧品牌区
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        
        JLabel title = new JLabel("NUTLLET");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        JLabel edition = new JLabel("Edition");
        edition.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        edition.setForeground(new Color(255, 255, 255, 150));
        edition.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        leftPanel.add(title);
        leftPanel.add(edition);

        // 右侧控制按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        String[] buttons = {"QSearch", "Enterprise", "Logout"};
        for (String btnText : buttons) {
            JButton btn = new JButton(btnText);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setForeground(btnText.equals("Enterprise") ? PRIMARY_PURPLE : Color.WHITE);
            btn.setBackground(btnText.equals("Enterprise") ? Color.WHITE : PRIMARY_PURPLE);
            btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            rightPanel.add(btn);
        }

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // 主内容区域（左右分割）
    private JSplitPane createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(650);
        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(createRightPanel());
        return splitPane;
    }

    // 左侧面板（饼图+余额）
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // 当前余额
        JLabel balance = new JLabel("<html><font color='#666666' size='4'>Current Balance</font><br/>"
            + "<font color='#333333' size='6'>¥ 8888.88</font></html>");
        balance.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(balance);

        // 饼状图
        panel.add(createPieChart());
        panel.add(Box.createVerticalStrut(20));

        // 进度条模块
        String[] progressItems = {
            "Loan Repayment Reminder:20%", 
            "Month Budget:65%", 
            "Present Planning:30%"
        };
        for (String item : progressItems) {
            panel.add(createProgressBar(item));
            panel.add(Box.createVerticalStrut(10));
        }

        // 底部按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.add(createButton("Modify Your Reminder", true));
        btnPanel.add(createButton("More Details", false));
        panel.add(btnPanel);
        
        return panel;
    }

    // 精确交易记录列表
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 0, 20, 20));
        panel.setBackground(Color.WHITE);

        // 标题栏
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Recent Consumption");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        
        JTextField search = new JTextField("Search transactions...", 15);
        search.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        header.add(title, BorderLayout.WEST);
        header.add(search, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // 交易条目
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
                return String.format("<html><div style='width:300px;padding:8px;border-bottom:1px solid #eeeeee'>"
                    + "<div style='display:flex;justify-content:space-between;color:%s'>"
                    + "<span>%s • %s</span><span>%s</span></div>"
                    + "<div style='color:#999999;font-size:12px;padding-top:4px'>%s</div></div>",
                    (i%2==0) ? "#8064E4" : "#333333",
                    transactions[i][0], transactions[i][1], transactions[i][2], transactions[i][3]);
            }
        });
        
        list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(index % 2 == 0 ? new Color(250, 250, 250) : Color.WHITE);
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
        
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    // 底部建议面板
    private JPanel createAdvicePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(230, 230, 230)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setBackground(new Color(248, 248, 248));
        
        JTextArea text = new JTextArea();
        text.setText("Based on my consumption, give me some advice.\n\n"
            + "Here are some suggestions based on your consumption. You can adjust and apply them according to\n"
            + "your actual situation:\n\n"
            + "General Advice\n"
            + "• Create budget: List income and expenses, allocate funds to categories\n"
            + "• Distinguish between needs and wants before purchases\n"
            + "• Research and compare products/services before buying\n\n"
            + "AI Consumption Analysis\n"
            + "Next period focus on grocery spending optimization. Consider meal prepping to better manage food budget.");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setForeground(new Color(64, 64, 64));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        
        panel.add(new JScrollPane(text), BorderLayout.CENTER);
        return panel;
    }

    // 实用方法
    private ChartPanel createPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Catering", 45);
        dataset.setValue("Living", 25);
        dataset.setValue("Entertainment", 15);
        dataset.setValue("Transportation", 10);
        dataset.setValue("Others", 5);

        JFreeChart chart = ChartFactory.createPieChart(null, dataset, false, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Catering", new Color(128, 100, 228));
        plot.setSectionPaint("Living", new Color(100, 200, 255));
        plot.setSectionPaint("Entertainment", new Color(255, 150, 150));
        plot.setSectionPaint("Transportation", new Color(150, 255, 150));
        plot.setSectionPaint("Others", new Color(255, 200, 150));
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 250));
        return chartPanel;
    }

    private JPanel createProgressBar(String text) {
        String[] parts = text.split(":");
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel label = new JLabel(parts[0]);
        label.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
        
        JProgressBar pb = new JProgressBar();
        pb.setValue(Integer.parseInt(parts[1].replace("%", "")));
        pb.setString(parts[1]);
        pb.setStringPainted(true);
        pb.setForeground(PRIMARY_PURPLE);
        pb.setBackground(LIGHT_PURPLE_BG);
        pb.setPreferredSize(new Dimension(400, 8));
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(pb, BorderLayout.SOUTH);
        return panel;
    }

    private JButton createButton(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", primary ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(primary ? Color.WHITE : PRIMARY_PURPLE);
        btn.setBackground(primary ? PRIMARY_PURPLE : Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 1, primary ? 2 : 1, 1, primary ? new Color(90, 70, 200) : Color.GRAY),
            new EmptyBorder(6, 20, 6, 20)
        ));
        return btn;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Nutllet();
    }
}