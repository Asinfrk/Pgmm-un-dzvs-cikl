# File Operations
# Writing to file
with open('sample.txt', 'w') as file:
    file.write('This is a sample file\n')
    file.write('Python is awesome!')

# Reading from file
with open('sample.txt', 'r') as file:
    content = file.read()
    print(content)
