import { Hono } from "npm:hono";
import { cors } from "npm:hono/cors";
import { logger } from "npm:hono/logger";
import { createClient } from "npm:@supabase/supabase-js";
import * as kv from "./kv_store.tsx";

const app = new Hono();

// Enable logger
app.use('*', logger(console.log));

// Enable CORS for all routes and methods
app.use(
  "/*",
  cors({
    origin: "*",
    allowHeaders: ["Content-Type", "Authorization"],
    allowMethods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    exposeHeaders: ["Content-Length"],
    maxAge: 600,
  }),
);

// Create Supabase client
const supabaseUrl = Deno.env.get('SUPABASE_URL') || '';
const supabaseServiceKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') || '';
const supabase = createClient(supabaseUrl, supabaseServiceKey);

// Initialize demo user on server start
async function initializeDemoUser() {
  try {
    console.log('Checking if demo user exists...');
    
    // Try to get existing demo user
    const { data: users } = await supabase.auth.admin.listUsers();
    const demoUserExists = users?.users?.some(user => user.email === 'demo@gastosapp.com');
    
    if (!demoUserExists) {
      console.log('Creating demo user...');
      const { data, error } = await supabase.auth.admin.createUser({
        email: 'demo@gastosapp.com',
        password: 'demo123456',
        user_metadata: { name: 'Usuario Demo' },
        email_confirm: true
      });
      
      if (error) {
        console.log('Error creating demo user:', error.message);
      } else {
        console.log('Demo user created successfully!');
        console.log('Email: demo@gastosapp.com');
        console.log('Password: demo123456');
      }
    } else {
      console.log('Demo user already exists');
    }
  } catch (error) {
    console.log('Error initializing demo user:', error);
  }
}

// Initialize demo user
initializeDemoUser();

// Health check endpoint
app.get("/make-server-aa79c66d/health", (c) => {
  return c.json({ status: "ok" });
});

// Register endpoint
app.post("/make-server-aa79c66d/register", async (c) => {
  try {
    const { name, email, password } = await c.req.json();

    // Validate required fields
    if (!name || !email || !password) {
      return c.json({ error: "Todos los campos son obligatorios" }, 400);
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return c.json({ error: "Formato de email inválido" }, 400);
    }

    // Create user with Supabase Auth
    const { data, error } = await supabase.auth.admin.createUser({
      email,
      password,
      user_metadata: { name },
      // Automatically confirm the user's email since an email server hasn't been configured.
      email_confirm: true
    });

    if (error) {
      console.log(`Error registering user: ${error.message}`);
      return c.json({ error: error.message }, 400);
    }

    return c.json({ 
      message: "Usuario registrado exitosamente",
      user: {
        id: data.user.id,
        email: data.user.email,
        name: data.user.user_metadata.name
      }
    }, 201);
  } catch (error) {
    console.log(`Error during registration: ${error}`);
    return c.json({ error: "Error al registrar usuario" }, 500);
  }
});

// Login endpoint
app.post("/make-server-aa79c66d/login", async (c) => {
  try {
    const { email, password } = await c.req.json();

    // Validate required fields
    if (!email || !password) {
      return c.json({ error: "Email y contraseña son obligatorios" }, 400);
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return c.json({ error: "Formato de email inválido" }, 400);
    }

    // Create a temporary client for sign in
    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL') || '',
      Deno.env.get('SUPABASE_ANON_KEY') || ''
    );

    const { data, error } = await supabaseClient.auth.signInWithPassword({
      email,
      password,
    });

    if (error) {
      console.log(`Error signing in user: ${error.message}`);
      return c.json({ error: "Credenciales inválidas" }, 401);
    }

    return c.json({
      message: "Inicio de sesión exitoso",
      access_token: data.session.access_token,
      user: {
        id: data.user.id,
        email: data.user.email,
        name: data.user.user_metadata.name
      }
    });
  } catch (error) {
    console.log(`Error during login: ${error}`);
    return c.json({ error: "Error al iniciar sesión" }, 500);
  }
});

// Get expenses summary (dashboard)
app.get("/make-server-aa79c66d/expenses/summary", async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    if (!user || authError) {
      return c.json({ error: "No autorizado" }, 401);
    }

    // Get current month expenses
    const now = new Date();
    const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1).toISOString();
    
    const expenses = await kv.getByPrefix(`expense:${user.id}:`);
    
    // Filter by current month and calculate total
    const monthlyExpenses = expenses.filter(expense => 
      expense.date >= firstDayOfMonth
    );
    
    const totalMonth = monthlyExpenses.reduce((sum, expense) => sum + expense.amount, 0);
    
    // Get recent expenses (last 5)
    const recentExpenses = expenses
      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
      .slice(0, 5);

    return c.json({
      totalMonth,
      recentExpenses,
      userName: user.user_metadata.name
    });
  } catch (error) {
    console.log(`Error getting expenses summary: ${error}`);
    return c.json({ error: "Error al obtener resumen de gastos" }, 500);
  }
});

// Get all expenses
app.get("/make-server-aa79c66d/expenses", async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    if (!user || authError) {
      return c.json({ error: "No autorizado" }, 401);
    }

    const expenses = await kv.getByPrefix(`expense:${user.id}:`);
    
    // Sort by date descending
    const sortedExpenses = expenses.sort((a, b) => 
      new Date(b.date).getTime() - new Date(a.date).getTime()
    );

    return c.json({ expenses: sortedExpenses });
  } catch (error) {
    console.log(`Error getting expenses: ${error}`);
    return c.json({ error: "Error al obtener gastos" }, 500);
  }
});

// Create expense
app.post("/make-server-aa79c66d/expenses", async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    if (!user || authError) {
      return c.json({ error: "No autorizado" }, 401);
    }

    const { description, amount, category, date } = await c.req.json();

    // Validate required fields
    if (!description || !amount || !category || !date) {
      return c.json({ error: "Todos los campos son obligatorios" }, 400);
    }

    // Validate amount
    if (amount <= 0) {
      return c.json({ error: "El monto debe ser mayor a cero" }, 400);
    }

    const expenseId = crypto.randomUUID();
    const expense = {
      id: expenseId,
      description,
      amount: parseFloat(amount),
      category,
      date,
      userId: user.id
    };

    await kv.set(`expense:${user.id}:${expenseId}`, expense);

    return c.json({ 
      message: "Gasto creado exitosamente",
      expense 
    }, 201);
  } catch (error) {
    console.log(`Error creating expense: ${error}`);
    return c.json({ error: "Error al crear gasto" }, 500);
  }
});

// Get single expense
app.get("/make-server-aa79c66d/expenses/:id", async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    if (!user || authError) {
      return c.json({ error: "No autorizado" }, 401);
    }

    const expenseId = c.req.param('id');
    const expense = await kv.get(`expense:${user.id}:${expenseId}`);

    if (!expense) {
      return c.json({ error: "Gasto no encontrado" }, 404);
    }

    return c.json({ expense });
  } catch (error) {
    console.log(`Error getting expense: ${error}`);
    return c.json({ error: "Error al obtener gasto" }, 500);
  }
});

// Update expense
app.put("/make-server-aa79c66d/expenses/:id", async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    if (!user || authError) {
      return c.json({ error: "No autorizado" }, 401);
    }

    const expenseId = c.req.param('id');
    const existingExpense = await kv.get(`expense:${user.id}:${expenseId}`);

    if (!existingExpense) {
      return c.json({ error: "Gasto no encontrado" }, 404);
    }

    const { description, amount, category, date } = await c.req.json();

    // Validate required fields
    if (!description || !amount || !category || !date) {
      return c.json({ error: "Todos los campos son obligatorios" }, 400);
    }

    // Validate amount
    if (amount <= 0) {
      return c.json({ error: "El monto debe ser mayor a cero" }, 400);
    }

    const updatedExpense = {
      ...existingExpense,
      description,
      amount: parseFloat(amount),
      category,
      date
    };

    await kv.set(`expense:${user.id}:${expenseId}`, updatedExpense);

    return c.json({ 
      message: "Gasto actualizado exitosamente",
      expense: updatedExpense 
    });
  } catch (error) {
    console.log(`Error updating expense: ${error}`);
    return c.json({ error: "Error al actualizar gasto" }, 500);
  }
});

// Delete expense
app.delete("/make-server-aa79c66d/expenses/:id", async (c) => {
  try {
    const accessToken = c.req.header('Authorization')?.split(' ')[1];
    
    const { data: { user }, error: authError } = await supabase.auth.getUser(accessToken);
    if (!user || authError) {
      return c.json({ error: "No autorizado" }, 401);
    }

    const expenseId = c.req.param('id');
    const existingExpense = await kv.get(`expense:${user.id}:${expenseId}`);

    if (!existingExpense) {
      return c.json({ error: "Gasto no encontrado" }, 404);
    }

    await kv.del(`expense:${user.id}:${expenseId}`);

    return c.json({ message: "Gasto eliminado exitosamente" });
  } catch (error) {
    console.log(`Error deleting expense: ${error}`);
    return c.json({ error: "Error al eliminar gasto" }, 500);
  }
});

Deno.serve(app.fetch);