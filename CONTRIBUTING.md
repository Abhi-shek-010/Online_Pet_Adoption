# Contributing to PawMatch

Thank you for your interest in contributing to **PawMatch**! We welcome contributions from the community. Please read this guide to understand how to contribute.

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on the code, not the person
- Help others learn and grow

## How to Contribute

### 1. **Report Bugs**
Found a bug? Create an issue with:
- Clear title describing the bug
- Steps to reproduce
- Expected vs. actual behavior
- Screenshots (if applicable)
- Environment details (OS, Java version, MySQL version)

### 2. **Suggest Enhancements**
Have an idea? Open an issue with:
- Clear description of the feature
- Use case and benefits
- Possible implementation approach
- Mockups (if applicable)

### 3. **Submit Code Changes**

#### Setup Development Environment
```bash
# Clone your fork
git clone https://github.com/yourusername/pawmatch.git
cd pawmatch

# Create feature branch
git checkout -b feature/your-feature-name

# Make changes and test
mvn clean test
mvn spring-boot:run

# Commit with clear messages
git commit -m "Add: Description of your feature"
git push origin feature/your-feature-name
```

#### Pull Request Guidelines
- **Title**: Clear and descriptive
- **Description**: What changes and why
- **Testing**: How you tested the changes
- **Related Issues**: Reference any related issues (#123)
- **Screenshots**: If UI changes

Example:
```
Add: Email notification for adoption applications

Changes:
- Created EmailService class for sending emails
- Added email templates for different notification types
- Updated AdoptionApplication to send emails on status change
- Added SMTP configuration to application.properties

Testing:
- Tested with Gmail SMTP server
- Verified email is sent when status changes
- Tested with invalid email addresses

Closes #45
```

## Coding Standards

### Java Code
```java
// Follow Java conventions
public class MyClass {
    private String name;
    
    public String getName() {
        return name;
    }
    
    // Javadoc for public methods
    /**
     * Description of what the method does
     * 
     * @param param1 description
     * @return description
     */
    public void myMethod(String param1) {
        // Implementation
    }
}
```

### Commit Messages
```
Type: Brief description (50 chars or less)

Detailed explanation of the change (72 chars per line)
- Point 1
- Point 2
- Point 3

Closes #<issue-number>
```

**Types**: Add, Fix, Update, Refactor, Remove, Docs

### Variable Naming
- **Classes**: `PascalCase` (UserDAO, PetService)
- **Methods**: `camelCase` (getUserById, createPet)
- **Constants**: `UPPER_CASE` (MAX_RETRIES, DB_TIMEOUT)
- **Variables**: `camelCase` (userName, petId)

## Testing

Before submitting a pull request:

```bash
# Run tests
mvn test

# Check code quality
mvn clean compile

# Build the project
mvn package

# Manual testing
mvn spring-boot:run
# Visit http://localhost:8080 and test features
```

## Documentation

If you add new features, please update:
- `README.md` - Add feature description
- Relevant markdown files in project root
- Inline code comments for complex logic
- Javadoc for public methods

## Questions?

- Check existing documentation and issues
- Open a discussion issue
- Contact the maintainer

---

**Thank you for contributing to PawMatch!** 🐾
