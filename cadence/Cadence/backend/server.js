const express = require('express');
const cors = require('cors');
const mysql = require('mysql2');
const jwt = require('jsonwebtoken');
const app = express();
const PORT = 3000;

// Secret key for JWT
const JWT_SECRET = 'your_secret_key_cadence_2024';

// Autoriser CORS
app.use(cors());

// Pour lire le JSON dans les requêtes
app.use(express.json());

const db = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: '',
    database: 'cadence'
  });

// Connect to database
db.connect((err) => {
  if (err) {
    console.error('Database connection failed:', err);
    process.exit(1);
  }
  console.log('Connected to MySQL database successfully');
});

// LOGIN ENDPOINT
app.post('/login', (req, res) => {
  const { username, email, password } = req.body;

  console.log('Login attempt with email:', email);

  if (!email || !password) {
    return res.status(400).json({ message: 'Email and password are required' });
  }

  // Query database for user - using your actual table structure
  const query = 'SELECT id_user, fullname, email, password, role, is_active FROM users WHERE email = ?';
  
  db.query(query, [email], (err, results) => {
    if (err) {
      console.error('Database error:', err);
      return res.status(500).json({ message: 'Server error' });
    }

    console.log('Query results for email:', email, 'Found:', results.length, 'records');

    // Check if user exists
    if (results.length === 0) {
      console.log('User not found for email:', email);
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    const user = results[0];
    console.log('User found:', user.email, 'Password in DB:', user.password, 'Password provided:', password);

    // Check password
    if (user.password !== password) {
      console.log('Password mismatch for user:', user.email);
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    // Check if user is active
    if (user.is_active !== 1 && user.is_active !== true) {
      console.log('User is not active:', user.email);
      return res.status(401).json({ message: 'User account is inactive' });
    }

    console.log('Login successful for user:', user.email);

    // Generate JWT token
    const token = jwt.sign(
      { id: user.id_user, email: user.email, role: user.role, sub: user.id_user },
      JWT_SECRET,
      { expiresIn: '24h' }
    );

    res.json({
      token: token,
      user: {
        id: user.id_user,
        email: user.email,
        fullname: user.fullname,
        role: user.role
      },
      message: 'Login successful'
    });
  });
});

// REGISTER ENDPOINT (basic implementation)
app.post('/register', (req, res) => {
  const { email, password, fullname, phone, address, country, username } = req.body;

  if (!email || !password || !fullname) {
    return res.status(400).json({ message: 'Email, password, and fullname are required' });
  }

  // Check if user already exists
  const checkQuery = 'SELECT * FROM users WHERE email = ?';
  db.query(checkQuery, [email], (err, results) => {
    if (err) {
      console.error('Database error:', err);
      return res.status(500).json({ message: 'Server error' });
    }

    if (results.length > 0) {
      return res.status(400).json({ message: 'Email already exists' });
    }

    // Insert new user with your table structure
    const insertQuery = 'INSERT INTO users (fullname, email, password, role, is_active, phone_number, username) VALUES (?, ?, ?, ?, ?, ?, ?)';
    db.query(insertQuery, [fullname, email, password, 'CLIENT', 1, phone || null, username || email], (err, result) => {
      if (err) {
        console.error('Database error:', err);
        return res.status(500).json({ message: 'Registration failed' });
      }

      const userId = result.insertId;
      const user = { id: userId, email, fullname, role: 'CLIENT' };

      // Generate JWT token
      const token = jwt.sign(
        { id: userId, email: email, role: 'CLIENT', sub: userId },
        JWT_SECRET,
        { expiresIn: '24h' }
      );

      res.json({
        token: token,
        user: user,
        message: 'Registration successful'
      });
    });
  });
});

// Simple route pour tester
app.get('/categories',  (req, res) => {
    const query = 'SELECT * FROM categories';
     db.query(query, (err, results) => {
      if (err) {
        console.error('Erreur lors de la récupération :', err);
        return res.status(500).send('Erreur de serveur');
      }
      res.json(results); // Envoie les résultats en JSON
    });
});


app.get('/productsByCategory/:categ', (req, res) => {
  const categoryId = req.params.categ;  // Récupérer l'ID de la catégorie depuis l'URL

  // Requête SQL pour récupérer les produits d'une catégorie avec leurs images
  const query = `
      SELECT products.*, GROUP_CONCAT(images.url) AS imageUrls
      FROM products
      LEFT JOIN images ON products.id = images.product
      WHERE products.category = ?
      GROUP BY products.id
  `;
  
  db.query(query, [categoryId], (err, results) => {
      if (err) {
          console.error('Erreur lors de la récupération des produits :', err);
          return res.status(500).send('Erreur de serveur');
      }
      
      // Transformer les résultats pour avoir un tableau d'objets d'images
      const productsWithImages = results.map(product => {
          const imageUrls = product.imageUrls ? product.imageUrls.split(',') : [];
          const images = imageUrls.map(url => ({ url })); // Transformer en tableau d'objets avec la clé `url`
          
          return {
              ...product,
              images: images  // Ajoute un champ `images` contenant un tableau d'objets avec les URLs
          };
      });
      
      res.json(productsWithImages);  // Envoie les produits avec leurs images sous le format demandé
  });
});


app.get('/products', (req, res) => {
  // Requête SQL pour récupérer tous les produits avec leurs images
  const query = `
      SELECT products.*, GROUP_CONCAT(images.url) AS imageUrls
      FROM products
      LEFT JOIN images ON products.id = images.product
      GROUP BY products.id
  `;
  
  db.query(query, (err, results) => {
      if (err) {
          console.error('Erreur lors de la récupération des produits :', err);
          return res.status(500).send('Erreur de serveur');
      }
      
      // Transformer les résultats pour avoir un tableau d'objets d'images
      const productsWithImages = results.map(product => {
          const imageUrls = product.imageUrls ? product.imageUrls.split(',') : [];
          const images = imageUrls.map(url => ({ url })); // Transformer en tableau d'objets avec la clé `url`
          
          return {
              ...product,
              images: images  // Ajoute un champ `images` contenant un tableau d'objets avec les URLs
          };
      });
      
      res.json(productsWithImages);  // Envoie les produits avec leurs images sous le format demandé
  });
});
// 
// Lancer le serveur
app.listen(PORT, () => {
  console.log(`Serveur backend écoute sur http://localhost:${PORT}`);
});
