import { useEffect, useMemo, useState } from "react";

const API_BASE = import.meta.env.VITE_API_URL || "/api/v1";;

const emptyExpense = {
  title: "",
  amount: "",
  category: "",
  date: new Date().toISOString().slice(0, 10),
};

function formatMoney(value) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(Number(value || 0));
}

function formatDate(date) {
  if (!date) return "";

  return new Date(`${date}T00:00:00`).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function getCategoryIcon(category) {
  const value = category?.toLowerCase() || "";

  if (value.includes("food") || value.includes("grocery")) return "🍔";
  if (value.includes("transport") || value.includes("travel")) return "🚗";
  if (value.includes("shop")) return "🛍️";
  if (value.includes("bill") || value.includes("utility")) return "💡";
  if (value.includes("health")) return "❤️";
  if (value.includes("entertain")) return "🎬";
  if (value.includes("education")) return "📚";
  if (value.includes("rent")) return "🏠";

  return "💳";
}

async function readResponse(response) {
  const contentType = response.headers.get("content-type") || "";

  const body = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message =
      typeof body === "string"
        ? body
        : body?.message || body?.error || "Request failed";

    throw new Error(message);
  }

  return body;
}

export default function App() {
  const [token, setToken] = useState(
    () => localStorage.getItem("spendwise_token") || ""
  );

  const [authMode, setAuthMode] = useState("login");

  const [authForm, setAuthForm] = useState({
    email: "",
    password: "",
  });

  const [expenses, setExpenses] = useState([]);
  const [expenseForm, setExpenseForm] = useState(emptyExpense);
  const [editingId, setEditingId] = useState(null);

  const [filters, setFilters] = useState({
    search: "",
    category: "",
    startDate: "",
    endDate: "",
    sortBy: "",
  });

  const [monthlyFilter, setMonthlyFilter] = useState({
    month: String(new Date().getMonth() + 1),
    year: String(new Date().getFullYear()),
  });

  const [categoryTotals, setCategoryTotals] = useState({});
  const [monthlyTotal, setMonthlyTotal] = useState(0);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const [showExpenseModal, setShowExpenseModal] = useState(false);
  const [showFilters, setShowFilters] = useState(false);

  const authHeaders = useMemo(
    () => ({
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    }),
    [token]
  );

  const totalSpend = useMemo(
    () =>
      expenses.reduce(
        (sum, expense) => sum + Number(expense.amount || 0),
        0
      ),
    [expenses]
  );

  const highestCategory = useMemo(() => {
    const entries = Object.entries(categoryTotals);

    if (!entries.length) return "No data";

    return entries.sort((a, b) => Number(b[1]) - Number(a[1]))[0][0];
  }, [categoryTotals]);

  const categoryEntries = useMemo(
    () =>
      Object.entries(categoryTotals).sort(
        (a, b) => Number(b[1]) - Number(a[1])
      ),
    [categoryTotals]
  );

  useEffect(() => {
    if (!token) return;

    loadExpenses();
    loadCategoryTotals();
    loadMonthlyTotal();
  }, [token]);

  async function request(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, options);
    return readResponse(response);
  }

  function showMessage(text) {
    setMessage(text);
    setError("");
  }

  function showError(err) {
    setError(err.message || "Something went wrong");
    setMessage("");
  }

  async function handleAuth(event) {
    event.preventDefault();
    setLoading(true);

    try {
      const result = await request(`/auth/${authMode}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(authForm),
      });

      if (authMode === "login") {
        localStorage.setItem("spendwise_token", result);
        setToken(result);
        showMessage("Welcome back!");
      } else {
        setAuthMode("login");
        showMessage("Account created. You can now log in.");
      }
    } catch (err) {
      showError(err);
    } finally {
      setLoading(false);
    }
  }

  async function loadExpenses(nextFilters = filters) {
    setLoading(true);

    try {
      const params = new URLSearchParams();

      if (nextFilters.startDate && nextFilters.endDate) {
        params.set("startDate", nextFilters.startDate);
        params.set("endDate", nextFilters.endDate);
      }

      if (nextFilters.sortBy) {
        params.set("sortBy", nextFilters.sortBy);
      }

      const list = await request(
        `/expenses${params.toString() ? `?${params}` : ""}`,
        {
          headers: authHeaders,
        }
      );

      setExpenses(Array.isArray(list) ? list : []);
    } catch (err) {
      showError(err);
    } finally {
      setLoading(false);
    }
  }

  async function loadCategory(categoryName) {
    if (!categoryName.trim()) {
      loadExpenses();
      return;
    }

    setLoading(true);

    try {
      const list = await request(
        `/expenses/category?categoryName=${encodeURIComponent(
          categoryName.trim()
        )}`,
        {
          headers: authHeaders,
        }
      );

      setExpenses(Array.isArray(list) ? list : []);
    } catch (err) {
      showError(err);
    } finally {
      setLoading(false);
    }
  }

  async function searchExpenses(title) {
    if (!title.trim()) {
      loadExpenses();
      return;
    }

    setLoading(true);

    try {
      const list = await request(
        `/expenses/search?title=${encodeURIComponent(title.trim())}`,
        {
          headers: authHeaders,
        }
      );

      setExpenses(Array.isArray(list) ? list : []);
    } catch (err) {
      showError(err);
    } finally {
      setLoading(false);
    }
  }

  async function loadCategoryTotals() {
    try {
      const totals = await request("/expenses/total/category", {
        headers: authHeaders,
      });

      setCategoryTotals(totals || {});
    } catch (err) {
      showError(err);
    }
  }

  async function loadMonthlyTotal(nextFilter = monthlyFilter) {
    try {
      const total = await request(
        `/expenses/total/monthly?month=${nextFilter.month}&year=${nextFilter.year}`,
        {
          headers: authHeaders,
        }
      );

      setMonthlyTotal(Number(total || 0));
    } catch (err) {
      showError(err);
    }
  }

  async function saveExpense(event) {
    event.preventDefault();

    const payload = {
      ...expenseForm,
      amount: Number(expenseForm.amount),
    };

    setLoading(true);

    try {
      if (editingId) {
        await request(`/expenses/${editingId}`, {
          method: "PUT",
          headers: authHeaders,
          body: JSON.stringify(payload),
        });

        showMessage("Expense updated successfully.");
      } else {
        await request("/expenses", {
          method: "POST",
          headers: authHeaders,
          body: JSON.stringify(payload),
        });

        showMessage("Expense added successfully.");
      }

      setExpenseForm(emptyExpense);
      setEditingId(null);
      setShowExpenseModal(false);

      await loadExpenses();
      await loadCategoryTotals();
      await loadMonthlyTotal();
    } catch (err) {
      showError(err);
    } finally {
      setLoading(false);
    }
  }

  function editExpense(expense) {
    setEditingId(expense.id);

    setExpenseForm({
      title: expense.title || "",
      amount: String(expense.amount || ""),
      category: expense.category || "",
      date: expense.date || emptyExpense.date,
    });

    setShowExpenseModal(true);
  }

  async function deleteExpense(id) {
    if (!window.confirm("Delete this expense?")) return;

    setLoading(true);

    try {
      await request(`/expenses/${id}`, {
        method: "DELETE",
        headers: authHeaders,
      });

      showMessage("Expense deleted.");

      await loadExpenses();
      await loadCategoryTotals();
      await loadMonthlyTotal();
    } catch (err) {
      showError(err);
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem("spendwise_token");

    setToken("");
    setExpenses([]);
    setCategoryTotals({});
    setMonthlyTotal(0);
  }

  function updateFilters(field, value) {
    setFilters((current) => ({
      ...current,
      [field]: value,
    }));
  }

  function applyFilters(event) {
    event.preventDefault();

    if (filters.search.trim()) {
      searchExpenses(filters.search);
      return;
    }

    if (filters.category.trim()) {
      loadCategory(filters.category);
      return;
    }

    loadExpenses(filters);
  }

  function resetFilters() {
    const nextFilters = {
      search: "",
      category: "",
      startDate: "",
      endDate: "",
      sortBy: "",
    };

    setFilters(nextFilters);
    loadExpenses(nextFilters);
  }

  function openAddExpense() {
    setEditingId(null);
    setExpenseForm(emptyExpense);
    setShowExpenseModal(true);
  }

  function closeModal() {
    setShowExpenseModal(false);
    setEditingId(null);
    setExpenseForm(emptyExpense);
  }

  if (!token) {
    return (
      <main className="auth-shell">
        <section className="auth-panel">
          <div className="auth-brand">
            <div className="brand-mark">S</div>

            <p className="eyebrow">SpendWise</p>

            <h1>
              Take control of
              <span> your spending.</span>
            </h1>

            <p className="auth-description">
              A simple and beautiful way to track your expenses, understand
              your habits, and stay on top of your money.
            </p>

            <div className="auth-features">
              <div>
                <span>✓</span>
                Track every expense
              </div>

              <div>
                <span>✓</span>
                Understand your spending
              </div>

              <div>
                <span>✓</span>
                Keep your finances organized
              </div>
            </div>
          </div>

          <form className="auth-form" onSubmit={handleAuth}>
            <div className="auth-heading">
              <h2>{authMode === "login" ? "Welcome back" : "Create account"}</h2>
              <p>
                {authMode === "login"
                  ? "Log in to continue to SpendWise."
                  : "Start managing your expenses today."}
              </p>
            </div>

            <div className="mode-switch">
              <button
                type="button"
                className={authMode === "login" ? "active" : ""}
                onClick={() => setAuthMode("login")}
              >
                Login
              </button>

              <button
                type="button"
                className={authMode === "register" ? "active" : ""}
                onClick={() => setAuthMode("register")}
              >
                Register
              </button>
            </div>

            <label>
              Email
              <input
                type="email"
                value={authForm.email}
                onChange={(event) =>
                  setAuthForm({
                    ...authForm,
                    email: event.target.value,
                  })
                }
                placeholder="you@example.com"
                required
              />
            </label>

            <label>
              Password
              <input
                type="password"
                value={authForm.password}
                onChange={(event) =>
                  setAuthForm({
                    ...authForm,
                    password: event.target.value,
                  })
                }
                placeholder="Enter your password"
                required
              />
            </label>

            {message && <p className="status success">{message}</p>}
            {error && <p className="status error">{error}</p>}

            <button className="primary-button auth-submit" type="submit">
              {loading
                ? "Please wait..."
                : authMode === "login"
                ? "Login to SpendWise"
                : "Create Account"}
            </button>
          </form>
        </section>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div className="topbar-inner">
          <div className="brand">
            <div className="brand-mark small">S</div>

            <div>
              <strong>SpendWise</strong>
              <span>Personal finance</span>
            </div>
          </div>

          <nav className="topbar-actions">
            <button className="header-add" onClick={openAddExpense}>
              <span>+</span>
              Add Expense
            </button>

            <button className="profile-button" onClick={logout}>
              Logout
            </button>
          </nav>
        </div>
      </header>

      <div className="dashboard">
        {(message || error) && (
          <div className={`status-banner ${error ? "error" : "success"}`}>
            <span>{error || message}</span>

            <button
              onClick={() => {
                setMessage("");
                setError("");
              }}
            >
              ×
            </button>
          </div>
        )}

        <section className="welcome-section">
          <div>
            <p className="eyebrow">Dashboard</p>
            <h1>Good morning 👋</h1>
            <p>Here's a quick look at your spending.</p>
          </div>

          <button className="primary-button desktop-add" onClick={openAddExpense}>
            + Add Expense
          </button>
        </section>

        <section className="summary-grid">
          <article className="metric primary-metric">
            <div className="metric-top">
              <span>This Month</span>
              <div className="metric-icon">₹</div>
            </div>

            <strong>{formatMoney(monthlyTotal)}</strong>

            <small>Current month spending</small>
          </article>

          <article className="metric">
            <div className="metric-top">
              <span>Visible Spend</span>
              <div className="metric-icon soft">₹</div>
            </div>

            <strong>{formatMoney(totalSpend)}</strong>

            <small>Based on current results</small>
          </article>

          <article className="metric">
            <div className="metric-top">
              <span>Transactions</span>
              <div className="metric-icon soft">↗</div>
            </div>

            <strong>{expenses.length}</strong>

            <small>Expenses shown</small>
          </article>

          <article className="metric">
            <div className="metric-top">
              <span>Top Category</span>
              <div className="metric-icon soft">
                {getCategoryIcon(highestCategory)}
              </div>
            </div>

            <strong className="category-value">{highestCategory}</strong>

            <small>Highest spending category</small>
          </article>
        </section>

        <section className="analytics-grid">
          <article className="dashboard-card overview-card">
            <div className="card-heading">
              <div>
                <h2>Spending Overview</h2>
                <p>Your spending for the selected month.</p>
              </div>

              <div className="month-selector">
                <select
                  value={monthlyFilter.month}
                  onChange={(event) => {
                    const next = {
                      ...monthlyFilter,
                      month: event.target.value,
                    };

                    setMonthlyFilter(next);
                    loadMonthlyTotal(next);
                  }}
                >
                  {Array.from({ length: 12 }, (_, index) => (
                    <option key={index + 1} value={index + 1}>
                      {new Date(2000, index).toLocaleString("en", {
                        month: "long",
                      })}
                    </option>
                  ))}
                </select>

                <input
                  type="number"
                  min="2000"
                  value={monthlyFilter.year}
                  onChange={(event) => {
                    const next = {
                      ...monthlyFilter,
                      year: event.target.value,
                    };

                    setMonthlyFilter(next);
                  }}
                  onBlur={() => loadMonthlyTotal()}
                />
              </div>
            </div>

            <div className="overview-content">
              <div className="big-number">{formatMoney(monthlyTotal)}</div>
              <span className="overview-label">total spending</span>

              <div className="fake-chart">
                <div className="chart-line">
                  <span />
                  <span />
                  <span />
                  <span />
                  <span />
                  <span />
                  <span />
                </div>

                <div className="chart-labels">
                  <span>Week 1</span>
                  <span>Week 2</span>
                  <span>Week 3</span>
                  <span>Week 4</span>
                </div>
              </div>
            </div>
          </article>

          <article className="dashboard-card category-card">
            <div className="card-heading">
              <div>
                <h2>Spending by Category</h2>
                <p>Where your money is going.</p>
              </div>

              <button
                className="icon-button"
                onClick={loadCategoryTotals}
                title="Refresh"
              >
                ↻
              </button>
            </div>

            {categoryEntries.length === 0 ? (
              <div className="category-empty">
                <span>📊</span>
                <p>No category data yet.</p>
              </div>
            ) : (
              <div className="category-list">
                {categoryEntries.slice(0, 5).map(([category, total]) => {
                  const max = Number(categoryEntries[0][1]) || 1;
                  const percentage =
                    (Number(total) / max) * 100;

                  return (
                    <div className="category-item" key={category}>
                      <div className="category-info">
                        <div className="category-name">
                          <span className="category-icon">
                            {getCategoryIcon(category)}
                          </span>

                          <span>{category}</span>
                        </div>

                        <strong>{formatMoney(total)}</strong>
                      </div>

                      <div className="progress-track">
                        <div
                          className="progress-bar"
                          style={{ width: `${percentage}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </article>
        </section>

        <section className="dashboard-card expenses-card">
          <div className="expenses-header">
            <div>
              <h2>Recent Expenses</h2>
              <p>Keep track of your latest transactions.</p>
            </div>

            <button
              className="text-button"
              onClick={() => loadExpenses()}
            >
              Refresh ↻
            </button>
          </div>

          <div className="expense-toolbar">
            <div className="search-box">
              <span>⌕</span>

              <input
                value={filters.search}
                onChange={(event) =>
                  updateFilters("search", event.target.value)
                }
                onKeyDown={(event) => {
                  if (event.key === "Enter") {
                    searchExpenses(filters.search);
                  }
                }}
                placeholder="Search expenses..."
              />
            </div>

            <button
              className={`filter-toggle ${showFilters ? "active" : ""}`}
              onClick={() => setShowFilters(!showFilters)}
            >
              ⚙ Filters
            </button>
          </div>

          {showFilters && (
            <form className="advanced-filters" onSubmit={applyFilters}>
              <label>
                Category
                <input
                  value={filters.category}
                  onChange={(event) =>
                    updateFilters("category", event.target.value)
                  }
                  placeholder="Food"
                />
              </label>

              <label>
                Start date
                <input
                  type="date"
                  value={filters.startDate}
                  onChange={(event) =>
                    updateFilters("startDate", event.target.value)
                  }
                />
              </label>

              <label>
                End date
                <input
                  type="date"
                  value={filters.endDate}
                  onChange={(event) =>
                    updateFilters("endDate", event.target.value)
                  }
                />
              </label>

              <label>
                Sort
                <select
                  value={filters.sortBy}
                  onChange={(event) =>
                    updateFilters("sortBy", event.target.value)
                  }
                >
                  <option value="">Newest default</option>
                  <option value="amount">Amount</option>
                  <option value="date">Date</option>
                  <option value="title">Title</option>
                  <option value="category">Category</option>
                </select>
              </label>

              <div className="filter-actions">
                <button className="secondary-button" type="submit">
                  Apply
                </button>

                <button
                  className="text-button"
                  type="button"
                  onClick={resetFilters}
                >
                  Reset
                </button>
              </div>
            </form>
          )}

          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Expense</th>
                  <th>Category</th>
                  <th>Date</th>
                  <th className="amount-cell">Amount</th>
                  <th />
                </tr>
              </thead>

              <tbody>
                {expenses.length === 0 ? (
                  <tr>
                    <td colSpan="5">
                      <div className="empty-state">
                        <div className="empty-icon">₹</div>
                        <h3>No expenses found</h3>
                        <p>
                          Add your first expense to start tracking your
                          spending.
                        </p>

                        <button
                          className="primary-button"
                          onClick={openAddExpense}
                        >
                          + Add Expense
                        </button>
                      </div>
                    </td>
                  </tr>
                ) : (
                  expenses.map((expense) => (
                    <tr key={expense.id}>
                      <td>
                        <div className="expense-name">
                          <div className="expense-icon">
                            {getCategoryIcon(expense.category)}
                          </div>

                          <div>
                            <strong>{expense.title}</strong>
                            <span>{expense.category}</span>
                          </div>
                        </div>
                      </td>

                      <td>
                        <span className="category-pill">
                          {expense.category}
                        </span>
                      </td>

                      <td className="date-cell">
                        {formatDate(expense.date)}
                      </td>

                      <td className="amount-cell expense-amount">
                        {formatMoney(expense.amount)}
                      </td>

                      <td>
                        <div className="row-actions">
                          <button
                            className="row-action"
                            onClick={() => editExpense(expense)}
                            title="Edit"
                          >
                            ✎
                          </button>

                          <button
                            className="row-action danger"
                            onClick={() => deleteExpense(expense.id)}
                            title="Delete"
                          >
                            ×
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      {showExpenseModal && (
        <div className="modal-overlay" onMouseDown={closeModal}>
          <div
            className="expense-modal"
            onMouseDown={(event) => event.stopPropagation()}
          >
            <div className="modal-heading">
              <div>
                <p className="eyebrow">
                  {editingId ? "Update transaction" : "New transaction"}
                </p>

                <h2>
                  {editingId ? "Edit Expense" : "Add Expense"}
                </h2>
              </div>

              <button className="close-button" onClick={closeModal}>
                ×
              </button>
            </div>

            <form onSubmit={saveExpense}>
              <label>
                What did you spend on?
                <input
                  value={expenseForm.title}
                  onChange={(event) =>
                    setExpenseForm({
                      ...expenseForm,
                      title: event.target.value,
                    })
                  }
                  placeholder="Groceries, Uber, Netflix..."
                  required
                  autoFocus
                />
              </label>

              <div className="form-row">
                <label>
                  Amount
                  <div className="amount-input">
                    <span>₹</span>

                    <input
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={expenseForm.amount}
                      onChange={(event) =>
                        setExpenseForm({
                          ...expenseForm,
                          amount: event.target.value,
                        })
                      }
                      placeholder="0.00"
                      required
                    />
                  </div>
                </label>

                <label>
                  Date
                  <input
                    type="date"
                    value={expenseForm.date}
                    onChange={(event) =>
                      setExpenseForm({
                        ...expenseForm,
                        date: event.target.value,
                      })
                    }
                    required
                  />
                </label>
              </div>

              <label>
                Category
                <input
                  value={expenseForm.category}
                  onChange={(event) =>
                    setExpenseForm({
                      ...expenseForm,
                      category: event.target.value,
                    })
                  }
                  placeholder="Food"
                  required
                />
              </label>

              <div className="modal-actions">
                <button
                  className="ghost-button"
                  type="button"
                  onClick={closeModal}
                >
                  Cancel
                </button>

                <button
                  className="primary-button"
                  type="submit"
                  disabled={loading}
                >
                  {loading
                    ? "Saving..."
                    : editingId
                    ? "Save Changes"
                    : "Add Expense"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </main>
  );
}
